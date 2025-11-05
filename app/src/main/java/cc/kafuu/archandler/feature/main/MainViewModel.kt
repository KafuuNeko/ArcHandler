package cc.kafuu.archandler.feature.main

import android.content.Context
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.about.AboutActivity
import cc.kafuu.archandler.feature.createarchive.CreateArchiveActivity
import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPackMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPasteMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainDialogState
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainLoadState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.presentation.MainViewEvent
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.archive.ArchiveManager
import cc.kafuu.archandler.libs.archive.IPasswordProvider
import cc.kafuu.archandler.libs.core.AppViewEvent
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.extensions.copyOrMoveTo
import cc.kafuu.archandler.libs.extensions.countAllFiles
import cc.kafuu.archandler.libs.extensions.createChooserIntent
import cc.kafuu.archandler.libs.extensions.createUniqueDirectory
import cc.kafuu.archandler.libs.extensions.deletes
import cc.kafuu.archandler.libs.extensions.getSameNameDirectory
import cc.kafuu.archandler.libs.extensions.hasUnmovableItems
import cc.kafuu.archandler.libs.extensions.listFilteredFiles
import cc.kafuu.archandler.libs.extensions.sha256Of
import cc.kafuu.archandler.libs.manager.CacheManager
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.AppCacheType
import cc.kafuu.archandler.libs.model.FileConflictStrategy
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.utils.Stack
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Path
import kotlin.coroutines.coroutineContext
import kotlin.io.path.Path

class MainViewModel : CoreViewModelWithEvent<MainUiIntent, MainUiState>(
    initStatus = MainUiState.None
), KoinComponent {
    // 压缩包管理器
    private val mArchiveManager by inject<ArchiveManager>()

    // 应用缓存管理器
    private val mCacheManager by inject<CacheManager>()

    // 数据传递管理器
    private val mDataTransferManager by inject<DataTransferManager>()

    // 列表状态栈
    private var mListStateStack: Stack<MainListState> = Stack()

    // 压缩包密码提供请求接口
    val mPasswordProvider = object : IPasswordProvider {
        override suspend fun getPassword(
            file: File
        ): String? = MainDialogState.PasswordInput(file = file).run {
            popupAwaitDialogResult { deferredResult.awaitCompleted() }
        }
    }

    /**
     * 展示错误消息
     */
    private suspend fun errorMessage(exception: Throwable?) {
        val message = exception?.message
            ?: get<AppLibs>().getString(R.string.unknown_error)
        AppViewEvent.PopupToastMessage(message).emit()
    }

    /**
     * 主页弹窗通用流程
     */
    private suspend fun <R> MainDialogState.popupAwaitDialogResult(
        onAwaitResult: suspend () -> R
    ): R? {
        val state = awaitStateOf<MainUiState.Normal>() {
            it.dialogState is MainDialogState.None
        }
        val result = state.run {
            copy(dialogState = this@popupAwaitDialogResult).setup()
            onAwaitResult()
        }
        state.setup()
        return result
    }

    /**
     * 压入新的列表状态，将旧列表状态入栈
     */
    private fun MainUiState.Normal.pushListState(
        newListState: MainListState
    ): MainUiState.Normal {
        if (listState !is MainListState.Undecided) mListStateStack.push(listState)
        return copy(listState = newListState)
    }

    /**
     * 刷新数据
     */
    private suspend fun MainUiState.Normal.refresh(): MainUiState.Normal {
        copy(loadState = MainLoadState.DirectoryLoading).setup()
        val newState = when (val listState = listState) {
            MainListState.Undecided -> this

            is MainListState.Directory -> {
                val directory = File(listState.directoryPath.toString())
                val result = runCatching {
                    withContext(Dispatchers.IO) { directory.listFilteredFiles() }
                }
                if (result.isFailure) errorMessage(result.exceptionOrNull())
                val files = result.getOrNull() ?: emptyList()
                copy(
                    listState = listState.copy(
                        files = files,
                        canRead = directory.canRead(),
                        canWrite = directory.canWrite()
                    ),
                    viewModeState = (viewModeState as? MainListViewModeState.MultipleSelect)?.let { viewMode ->
                        val selected =
                            files.mapNotNull { file -> file.takeIf { viewMode.selected.contains(it) } }
                        viewMode.copy(selected = selected.toSet())
                    } ?: viewModeState,
                )
            }

            is MainListState.StorageVolume -> {
                val result = runCatching {
                    withContext(Dispatchers.IO) { get<FileManager>().getMountedStorageVolumes() }
                }
                if (result.isFailure) errorMessage(result.exceptionOrNull())
                val storageVolumes = result.getOrNull() ?: emptyList()
                val listState = MainListState.StorageVolume(
                    storageVolumes = storageVolumes
                )
                copy(listState = listState)
            }
        }
        // Reset state
        setup()
        return newState
    }

    /**
     * 进入一个目录
     */
    private suspend fun MainUiState.Normal.enterDirectory(
        storageData: StorageData,
        directoryPath: Path
    ): MainUiState.Normal {
        if (listState is MainListState.Directory) {
            val isRepeated = listOf(
                listState.directoryPath == directoryPath,
                listState.storageData.name == storageData.name,
                listState.storageData.directory.path == storageData.directory.path
            ).all { it }
            if (isRepeated) return refresh()
        }
        val newListState = MainListState.Directory(
            storageData = storageData,
            directoryPath = directoryPath
        )
        return pushListState(newListState).refresh()
    }

    /**
     * 加载挂载的存储设备
     */
    private suspend fun MainUiState.Normal.loadExternalStorages(): MainUiState.Normal {
        return pushListState(MainListState.StorageVolume()).refresh()
    }

    /**
     * 状态返回操作逻辑
     */
    private suspend fun MainUiState.Normal.backState(): MainUiState {
        var newListState = mListStateStack.popOrNull()
        var uiState = this
        if (newListState == null) {
            val restoreStack = when (viewModeState) {
                is MainListViewModeState.Pack -> viewModeState.restoreStack
                is MainListViewModeState.Paste -> viewModeState.restoreStack
                else -> null
            }
            if (restoreStack != null) {
                mListStateStack = restoreStack
                newListState = mListStateStack.popOrNull()
                uiState = uiState.copy(viewModeState = MainListViewModeState.Normal)
            }
        }
        return if (newListState != null) {
            uiState.copy(listState = newListState).refresh()
        } else {
            MainUiState.Finished
        }
    }

    /**
     * 启动压缩包解压任务
     */
    private suspend fun MainUiState.Normal.doFileExtract(file: File): File? {
        // 尝试打开压缩包
        copy(loadState = MainLoadState.ArchiveOpening(file)).setup()
        // 开始压缩包解压流程
        val dest = runCatching {
            mArchiveManager.openArchive(file)?.run {
                if (!open(mPasswordProvider)) null else this
            }?.use { archive ->
                val destDir = file.getSameNameDirectory().createUniqueDirectory() ?: return@use null
                archive.extractAll(destDir) { index, path, target ->
                    copy(loadState = MainLoadState.Unpacking(file, index, path, target))
                        .setup()
                }
                return@use destDir
            }
        }.getOrNull()
        // 清理缓存
        mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
        // 再次验证协程是否正在进行
        coroutineContext.ensureActive()
        // 解压流程完成，重置状态
        setup()
        if (dest == null) {
            // 解压失败
            AppViewEvent
                .PopupToastMessageByResId(R.string.archive_unpacking_failed_message)
                .emit()
        }
        return dest
    }

    /**
     * 切换入文件粘贴模式
     */
    private suspend fun MainUiState.Normal.enterPasteMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
        isMoving: Boolean = false
    ): MainUiState.Normal? {
        if (sourceFiles.isEmpty()) {
            val message = get<Context>().getString(R.string.enter_paste_is_empty_message)
            AppViewEvent.PopupToastMessage(message).emit()
            return null
        }
        val viewModeState = MainListViewModeState.Paste(
            sourceStorageData = sourceStorageData,
            sourceDirectoryPath = sourceDirectoryPath,
            sourceFiles = sourceFiles,
            isMoving = isMoving,
            restoreStack = Stack(mListStateStack).apply { push(listState) }
        )
        return copy(viewModeState = viewModeState)
    }

    /**
     * 切换入打包模式
     */
    private suspend fun MainUiState.Normal.enterPackMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
    ): MainUiState.Normal? {
        if (sourceFiles.isEmpty()) {
            val message = get<Context>().getString(R.string.enter_pack_is_empty_message)
            AppViewEvent.PopupToastMessage(message).emit()
            return null
        }
        val viewModeState = MainListViewModeState.Pack(
            sourceStorageData = sourceStorageData,
            sourceDirectoryPath = sourceDirectoryPath,
            sourceFiles = sourceFiles,
            restoreStack = Stack(mListStateStack).apply { push(listState) }
        )
        return copy(viewModeState = viewModeState)
    }

    /**
     * 重置视图模式为通常模式
     */
    suspend fun MainUiState.Normal.resetViewMode(): MainUiState.Normal {
        val restoreStack = when (viewModeState) {
            is MainListViewModeState.Pack -> viewModeState.restoreStack
            is MainListViewModeState.Paste -> viewModeState.restoreStack
            else -> null
        }
        if (restoreStack != null) {
            mListStateStack = restoreStack
        }
        return copy(
            viewModeState = MainListViewModeState.Normal,
            listState = restoreStack?.pop() ?: listState
        ).refresh()
    }

    /**
     * 执行删除文件操作
     */
    private suspend fun MainUiState.Normal.doDeleteFiles(
        fileSet: Set<File>
    ): Boolean = withContext(Dispatchers.IO) {
        if (fileSet.isEmpty()) {
            val message = get<Context>().getString(R.string.enter_paste_is_empty_message)
            AppViewEvent.PopupToastMessage(message).emit()
            return@withContext false
        }
        // 询问用户是否确认删除
        val isAgree = MainDialogState
            .FileDeleteConfirm(fileSet)
            .run { popupAwaitDialogResult { deferredResult.awaitCompleted() } } == true
        if (!isAgree) return@withContext false
        // 执行文件删除逻辑
        fileSet
            .toList()
            .deletes(onStartDelete = { copy(loadState = MainLoadState.FilesDeleting(it)).setup() })
        // 重置状态
        setup()
        return@withContext true
    }

    /**
     * 粘贴文件
     */
    private suspend fun MainUiState.Normal.doPasteFiles(
        targetDirectoryPath: Path
    ) = withContext(Dispatchers.IO) {
        val viewMode = viewModeState as? MainListViewModeState.Paste ?: return@withContext
        MainLoadState.FileScanning.run { copy(loadState = this) }.setup()
        // 验证目标目录是否允许写入
        val targetDirectoryFile = File(targetDirectoryPath.toString())
        if (!targetDirectoryFile.canWrite()) {
            AppViewEvent.PopupToastMessageByResId(R.string.cannot_write_directory_message).emit()
            setup()
            return@withContext
        }
        // 如果是移动文件，则验证源文件是否可被移动
        if (viewMode.isMoving && viewMode.sourceFiles.hasUnmovableItems()) {
            AppViewEvent.PopupToastMessageByResId(R.string.has_unmovable_files_message).emit()
            setup()
            return@withContext
        }
        val totality = viewMode.sourceFiles.sumOf { it.countAllFiles() }
        var currentIndex = 0
        var fileConflictStrategy: FileConflictStrategy? = null
        viewMode.sourceFiles.copyOrMoveTo(
            target = targetDirectoryFile,
            isMove = viewMode.isMoving,
            onStart = { src, dst ->
                MainLoadState
                    .Pasting(
                        isMoving = viewMode.isMoving,
                        src = src, dest = dst, totality = totality,
                        currentIndex = currentIndex++
                    )
                    .run { copy(loadState = this) }
                    .setup()
            },
            onConflict = { src, dst ->
                fileConflictStrategy?.run { return@copyOrMoveTo this }
                val result = MainDialogState.FileConflict(oldFile = src, newFile = dst).run {
                    popupAwaitDialogResult { deferredResult.awaitCompleted() }
                }
                val currentConflict = result?.first ?: FileConflictStrategy.Skip
                if (result?.second == true) fileConflictStrategy = currentConflict
                return@copyOrMoveTo currentConflict
            }
        )
        // 流程结束，重置状态
        setup()
    }


    /**
     * 页面初始化
     */
    @UiIntentObserver(MainUiIntent.Init::class)
    private suspend fun onInit() {
        // 只有在空页或者未授权状态下才可继续执行
        if (!isStateOf<MainUiState.None>() && !isStateOf<MainUiState.PermissionDenied>()) return
        // 清理缓存
        mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.PermissionDenied.setup()
        } else {
            MainUiState.Normal().loadExternalStorages().setup()
        }
    }

    @UiIntentObserver(MainUiIntent.Resume::class)
    private suspend fun onResume() {
        getOrNull<MainUiState.Normal>()?.refresh()?.setup()
    }

    /**
     * 页面返回逻辑
     */
    @UiIntentObserver(MainUiIntent.Back::class)
    private suspend fun onBack() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        if (uiState.loadState !is MainLoadState.None) return
        when (uiState.viewModeState) {
            is MainListViewModeState.MultipleSelect -> uiState.resetViewMode().setup()
            else -> uiState.backState().setup()
        }
    }


    /**
     * 跳转到文件权限设置
     */
    @UiIntentObserver(MainUiIntent.JumpFilePermissionSetting::class)
    private suspend fun onJumpFilePermissionSetting() {
        MainViewEvent.JumpFilePermissionSetting.emit()
    }

    /**
     * 处理主页抽屉按钮点击事件
     */
    @UiIntentObserver(MainUiIntent.MainDrawerMenuClick::class)
    private suspend fun onMainDrawerMenuClick(intent: MainUiIntent.MainDrawerMenuClick) {
        if (!isStateOf<MainUiState.Normal>()) return
        when (intent.menu) {
            MainDrawerMenuEnum.Code -> {
                get<AppLibs>().jumpToUrl(AppModel.CODE_REPOSITORY_URL)
            }

            MainDrawerMenuEnum.Feedback -> {
                get<AppLibs>().jumpToUrl(AppModel.FEEDBACK_URL)
            }

            MainDrawerMenuEnum.Rate -> {
                get<AppLibs>().jumpToUrl(AppModel.GOOGLE_PLAY_URL)
            }

            MainDrawerMenuEnum.About -> AppViewEvent.StartActivity(AboutActivity::class.java).emit()
        }
    }

    /**
     * 用户选择存储设备
     */
    @UiIntentObserver(MainUiIntent.StorageVolumeSelected::class)
    private suspend fun onStorageVolumeSelected(intent: MainUiIntent.StorageVolumeSelected) {
        getOrNull<MainUiState.Normal>()
            ?.enterDirectory(
                storageData = intent.storageData,
                directoryPath = Path(intent.storageData.directory.path)
            )
            ?.setup()
    }

    /**
     * 用户选择文件
     */
    @UiIntentObserver(MainUiIntent.FileSelected::class)
    private suspend fun onFileSelected(intent: MainUiIntent.FileSelected) {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return

        // 文件多选模式用户选择行为为切换选中模式
        if (uiState.viewModeState is MainListViewModeState.MultipleSelect) {
            val viewMode = uiState.viewModeState
            if (!listState.files.contains(intent.file)) return
            val selected = viewMode.selected.toMutableSet().apply {
                if (viewMode.selected.contains(intent.file)) remove(intent.file) else add(intent.file)
            }
            uiState.copy(viewModeState = viewMode.copy(selected = selected)).setup()
            return
        }
        // 如果用户选择的是目录则切换进目录
        if (intent.file.isDirectory) {
            uiState.enterDirectory(
                storageData = intent.storageData,
                directoryPath = Path(intent.file.path)
            ).setup()
            return
        }
        // 判断当前打开的文件是否可解压
        if (!ArchiveManager.isExtractable(intent.file)) {
            get<Context>().createChooserIntent(intent.file.name, intent.file)
                ?.let { AppViewEvent.StartActivityByIntent(it) }
                ?.emit()
        } else enqueueAsyncTask {
            val dest = uiState.doFileExtract(intent.file)
            if (dest == null) {
                AppViewEvent
                    .PopupToastMessageByResId(R.string.archive_unpacking_failed_message)
                    .emit()
            } else {
                uiState
                    .enterDirectory(intent.storageData, Path(dest.path))
                    .setup()
            }
        }
    }


    /**
     * 中断压缩包解压流程
     */
    @UiIntentObserver(MainUiIntent.CancelUnpackingJob::class)
    private suspend fun onCancelUnpackingJob() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        if (uiState.loadState is MainLoadState.Unpacking) {
            cancelActiveTaskAndRestore()
            mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
            uiState.refresh().setup()
        }
    }

    /**
     * 切换用户多选模式
     */
    @UiIntentObserver(MainUiIntent.FileMultipleSelectMode::class)
    private fun onFileMultipleSelectMode(intent: MainUiIntent.FileMultipleSelectMode) {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        when (uiState.viewModeState) {
            is MainListViewModeState.Paste -> return
            else -> Unit
        }
        uiState.copy(
            viewModeState = if (intent.enable) {
                MainListViewModeState.MultipleSelect(intent.file?.let { setOf(it) } ?: emptySet())
            } else {
                MainListViewModeState.Normal
            }
        ).setup()
    }

    /**
     * 文件多选模式底部菜单点击事件
     */
    @UiIntentObserver(MainUiIntent.MultipleMenuClick::class)
    private suspend fun onMultipleMenuClick(intent: MainUiIntent.MultipleMenuClick) {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val viewModel = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        when (intent.menu) {
            MainMultipleMenuEnum.Copy -> uiState.enterPasteMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles,
                isMoving = false
            )?.setup()

            MainMultipleMenuEnum.Move -> uiState.enterPasteMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles,
                isMoving = true
            )?.setup()

            MainMultipleMenuEnum.Delete -> {
                if (uiState.doDeleteFiles(viewModel.selected)) uiState.refresh().setup()
            }

            MainMultipleMenuEnum.Archive -> uiState.enterPackMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles
            )?.setup()
        }
    }


    /**
     * 粘贴模式底部菜单被点击
     */
    @UiIntentObserver(MainUiIntent.PasteMenuClick::class)
    private suspend fun onPasteMenuClick(intent: MainUiIntent.PasteMenuClick) {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        when (intent.menu) {
            MainPasteMenuEnum.Paste -> {
                uiState.doPasteFiles(targetDirectoryPath = intent.targetDirectoryPath)
                uiState
                    .resetViewMode()
                    .enterDirectory(intent.targetStorageData, intent.targetDirectoryPath)
                    .setup()
            }

            MainPasteMenuEnum.Cancel -> {
                uiState.resetViewMode().setup()
            }
        }
    }

    /**
     * 打包菜单被选择
     */
    @UiIntentObserver(MainUiIntent.PackMenuClick::class)
    private suspend fun onPackMenuClick(
        intent: MainUiIntent.PackMenuClick
    ) {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val viewMode = uiState.viewModeState as? MainListViewModeState.Pack ?: return
        when (intent.menu) {
            MainPackMenuEnum.Pack -> {
                val params = CreateArchiveActivity.params(
                    dtm = mDataTransferManager,
                    files = viewMode.sourceFiles.toList(),
                    targetStorageData = intent.targetStorageData,
                    targetDirectoryPath = intent.targetDirectoryPath
                )
                AppViewEvent.StartActivity(
                    activity = CreateArchiveActivity::class.java,
                    extras = params
                ).emit()
                uiState
                    .resetViewMode()
                    .enterDirectory(intent.targetStorageData, intent.targetDirectoryPath)
                    .setup()
            }

            MainPackMenuEnum.Cancel -> {
                uiState.resetViewMode().setup()
            }
        }
    }

    @UiIntentObserver(MainUiIntent.SelectAllClick::class)
    private fun onSelectAllClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        uiState.copy(viewModeState = viewModeState.copy(selected = listState.files.toSet())).setup()
    }

    @UiIntentObserver(MainUiIntent.DeselectClick::class)
    private fun onDeselectClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        uiState.copy(viewModeState = viewModeState.copy(selected = emptySet())).setup()
    }

    @UiIntentObserver(MainUiIntent.SelectAllNoDuplicatesClick::class)
    private suspend fun onSelectAllNoDuplicatesClick() = enqueueAsyncTask {
        // @formatter:off
        val uiState = getOrNull<MainUiState.Normal>() ?: return@enqueueAsyncTask
        val listState = uiState.listState as? MainListState.Directory ?: return@enqueueAsyncTask
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return@enqueueAsyncTask
        // @formatter:on
        if (uiState.loadState !is MainLoadState.None) return@enqueueAsyncTask
        val fileMap = hashMapOf<String, File>()
        uiState.copy(loadState = MainLoadState.QueryDuplicateFiles()).setup()
        listState.files.forEach { file ->
            if (!(file.isFile && file.canRead())) return@forEach
            uiState.copy(loadState = MainLoadState.QueryDuplicateFiles(file)).setup()
            file.sha256Of().run {
                if (fileMap.contains(this)) return@forEach
                fileMap[this] = file
            }
            coroutineContext.ensureActive()
        }
        uiState.copy(
            viewModeState = viewModeState.copy(selected = fileMap.map { it.value }.toSet())
        ).setup()
    }

    @UiIntentObserver(MainUiIntent.CancelSelectNoDuplicatesJob::class)
    private suspend fun onCancelSelectNoDuplicatesJob() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        if (uiState.loadState is MainLoadState.QueryDuplicateFiles) cancelActiveTaskAndRestore()
    }

    @UiIntentObserver(MainUiIntent.InvertSelectionClick::class)
    private fun onInvertSelectionClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        val all = listState.files.toSet()
        val inverted = all - viewModeState.selected
        uiState.copy(viewModeState = viewModeState.copy(selected = inverted)).setup()
    }

    @UiIntentObserver(MainUiIntent.CreateDirectoryClick::class)
    private suspend fun onCreateDirectoryClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        val directoryName = MainDialogState.CreateDirectoryInput().run {
            popupAwaitDialogResult { deferredResult.awaitCompleted() }
        } ?: return
        val directory = File(listState.directoryPath.toString(), directoryName)
        if (directory.exists()) {
            AppViewEvent.PopupToastMessageByResId(R.string.directory_already_exists).emit()
            return
        }
        if (directory.mkdirs()) {
            uiState.refresh().setup()
            AppViewEvent.PopupToastMessageByResId(R.string.directory_created_successfully).emit()
        } else {
            AppViewEvent.PopupToastMessageByResId(R.string.failed_to_create_directory).emit()
        }
    }

    @UiIntentObserver(MainUiIntent.RenameClick::class)
    private suspend fun onRenameClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        val file = viewModeState.selected.takeIf { it.isNotEmpty() }?.first() ?: return
        val parent = file.parent ?: return
        val newFileName = MainDialogState.RenameInput(file.name).run {
            popupAwaitDialogResult { deferredResult.awaitCompleted() }
        } ?: return
        val newFile = File(parent, newFileName)
        if (newFile.exists()) {
            AppViewEvent.PopupToastMessageByResId(R.string.file_already_exists).emit()
        } else {
            file.renameTo(newFile)
            uiState.refresh().setup()
        }
    }
}