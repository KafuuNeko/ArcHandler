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
import cc.kafuu.archandler.libs.extensions.appCopyTo
import cc.kafuu.archandler.libs.extensions.appMoveTo
import cc.kafuu.archandler.libs.extensions.createChooserIntent
import cc.kafuu.archandler.libs.extensions.createUniqueDirectory
import cc.kafuu.archandler.libs.extensions.deletes
import cc.kafuu.archandler.libs.extensions.generateUniqueFile
import cc.kafuu.archandler.libs.extensions.getParentPath
import cc.kafuu.archandler.libs.extensions.getSameNameDirectory
import cc.kafuu.archandler.libs.extensions.isSameFileOrDirectory
import cc.kafuu.archandler.libs.extensions.listFilteredFiles
import cc.kafuu.archandler.libs.extensions.sha256Of
import cc.kafuu.archandler.libs.manager.CacheManager
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.AppCacheType
import cc.kafuu.archandler.libs.model.FileConflictStrategy
import cc.kafuu.archandler.libs.model.StorageData
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
import kotlin.io.path.Path

class MainViewModel : CoreViewModelWithEvent<MainUiIntent, MainUiState>(
    initStatus = MainUiState.None
), KoinComponent {
    // 应用通用工具
    private val mAppLibs by inject<AppLibs>()

    // 压缩包管理器
    private val mArchiveManager by inject<ArchiveManager>()

    // 应用缓存管理器
    private val mCacheManager by inject<CacheManager>()

    // 数据传递管理器
    private val mDataTransferManager by inject<DataTransferManager>()

    // 压缩包密码提供请求接口
    val mPasswordProvider = object : IPasswordProvider {
        override suspend fun getPassword(
            file: File
        ): String? = MainDialogState.PasswordInput(file = file).run {
            popupAwaitDialogResult { deferredResult.awaitCompleted() }
        }
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
            loadExternalStorages()
        }
    }

    @UiIntentObserver(MainUiIntent.Resume::class)
    private suspend fun onResume() {
        doRefreshDirectory()
    }

    private suspend fun doRefreshDirectory() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        val multipleSelectMode = uiState.viewModeState as? MainListViewModeState.MultipleSelect

        uiState.copy(loadState = MainLoadState.DirectoryLoading).setup()
        val files = runCatching {
            withContext(Dispatchers.IO) {
                File(listState.directoryPath.toString()).listFilteredFiles()
            }
        }.getOrNull() ?: emptyList()

        val viewModeState = multipleSelectMode?.let { viewMode ->
            val selected =
                files.mapNotNull { file -> file.takeIf { viewMode.selected.contains(it) } }
            viewMode.copy(selected = selected.toSet())
        } ?: uiState.viewModeState

        uiState.copy(
            listState = listState.copy(files = files),
            viewModeState = viewModeState
        ).setup()
    }

    /**
     * 页面返回逻辑
     */
    @UiIntentObserver(MainUiIntent.Back::class)
    private suspend fun onBack() {
        val state = getOrNull<MainUiState.Normal>() ?: return
        if (state.loadState !is MainLoadState.None) return

        when (val viewMode = state.viewModeState) {
            MainListViewModeState.Normal -> {
                if (state.listState is MainListState.Directory) {
                    doBackToParent(state.listState.storageData, state.listState.directoryPath)
                } else {
                    MainUiState.Finished.setup()
                }
            }

            is MainListViewModeState.MultipleSelect -> {
                state.copy(viewModeState = MainListViewModeState.Normal).setup()
            }

            is MainListViewModeState.Paste -> {
                if (state.listState is MainListState.Directory) {
                    doBackToParent(state.listState.storageData, state.listState.directoryPath)
                } else {
                    doLoadDirectory(viewMode.sourceStorageData, viewMode.sourceDirectoryPath)
                }
            }

            is MainListViewModeState.Pack -> {
                if (state.listState is MainListState.Directory) {
                    doBackToParent(state.listState.storageData, state.listState.directoryPath)
                } else {
                    doLoadDirectory(viewMode.sourceStorageData, viewMode.sourceDirectoryPath)
                }
            }
        }
    }

    /**
     * 返回到上一级目录
     */
    private suspend fun doBackToParent(
        storageData: StorageData,
        currentPath: Path
    ) {
        val state = getOrNull<MainUiState.Normal>() ?: return
        val parent = currentPath.getParentPath()
        if (parent == null || Path(storageData.directory.path).isSameFileOrDirectory(currentPath)) {
            loadExternalStorages(state.viewModeState)
        } else {
            doLoadDirectory(storageData, parent, state.viewModeState)
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
        val state = getOrNull<MainUiState.Normal>() ?: return
        doLoadDirectory(
            storageData = intent.storageData,
            directoryPath = Path(intent.storageData.directory.path),
            viewModeState = state.viewModeState
        )
    }

    /**
     * 用户选择文件
     */
    @UiIntentObserver(MainUiIntent.FileSelected::class)
    private suspend fun onFileSelected(intent: MainUiIntent.FileSelected) {
        val state = getOrNull<MainUiState.Normal>() ?: return

        // 文件多选模式用户选择行为为切换选中模式
        if (state.viewModeState is MainListViewModeState.MultipleSelect) {
            val viewMode = state.viewModeState
            val selected = viewMode.selected.toMutableSet().apply {
                if (viewMode.selected.contains(intent.file)) remove(intent.file) else add(intent.file)
            }
            state.copy(viewModeState = viewMode.copy(selected = selected)).setup()
            return
        }
        // 如果用户选择的是目录则切换进目录
        if (intent.file.isDirectory) {
            doLoadDirectory(
                storageData = intent.storageData,
                directoryPath = Path(intent.file.path),
                viewModeState = state.viewModeState
            )
            return
        }
        // 判断当前打开的文件是否可解压
        if (!ArchiveManager.isExtractable(intent.file)) {
            get<Context>().createChooserIntent(intent.file.name, intent.file)
                ?.let { AppViewEvent.StartActivityByIntent(it) }
                ?.emit()
        } else {
            startFilePacking(intent.file)
        }
    }

    /**
     * 启动压缩包解压任务
     */
    private suspend fun startFilePacking(
        file: File
    ) = enqueueAsyncTask(Dispatchers.IO) {
        val state = getOrNull<MainUiState.Normal>() ?: return@enqueueAsyncTask
        val listState = state.listState as? MainListState.Directory ?: return@enqueueAsyncTask
        // 尝试打开压缩包
        state.copy(loadState = MainLoadState.ArchiveOpening(file)).setup()
        // 开始压缩包解压流程
        val dest = runCatching {
            mArchiveManager.openArchive(file)?.run {
                if (!open(mPasswordProvider)) null else this
            }?.use { archive ->
                val destDir = file.getSameNameDirectory().createUniqueDirectory() ?: return@use null
                archive.extractAll(destDir) { index, path, target ->
                    state.copy(loadState = MainLoadState.Unpacking(file, index, path, target))
                        .setup()
                }
                return@use destDir
            }
        }.getOrNull()
        // 清理缓存
        mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
        // 再次验证协程是否正在进行
        coroutineContext.ensureActive()
        // 解压流程完成，取消加载弹窗
        state.copy(loadState = MainLoadState.None).setup()
        if (dest == null) {
            // 解压失败
            AppViewEvent.PopupToastMessage(
                mAppLibs.getString(R.string.archive_unpacking_failed_message)
            ).emit()
            doRefreshDirectory()
        } else {
            // 解压成功
            doLoadDirectory(listState.storageData, Path(dest.path))
        }
    }

    /**
     * 中断压缩包解压流程
     */
    @UiIntentObserver(MainUiIntent.CancelUnpackingJob::class)
    private suspend fun onCancelUnpackingJob() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        if (uiState.loadState is MainLoadState.Unpacking) {
            cancelActiveTaskAndRestore()
            mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
            doRefreshDirectory()
        }
    }

    /**
     * 切换用户多选模式
     */
    @UiIntentObserver(MainUiIntent.FileMultipleSelectMode::class)
    private fun onFileMultipleSelectMode(intent: MainUiIntent.FileMultipleSelectMode) {
        val state = getOrNull<MainUiState.Normal>() ?: return
        when (state.viewModeState) {
            is MainListViewModeState.Paste -> return
            else -> Unit
        }
        state.copy(
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
        val state = getOrNull<MainUiState.Normal>() ?: return
        val listState = state.listState as? MainListState.Directory ?: return
        val viewModel = state.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        when (intent.menu) {
            MainMultipleMenuEnum.Copy -> doEntryPasteMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles,
                isMoving = false
            )

            MainMultipleMenuEnum.Move -> doEntryPasteMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles,
                isMoving = true
            )

            MainMultipleMenuEnum.Delete -> withContext(Dispatchers.IO) {
                if (doDeleteFiles(viewModel.selected)) {
                    doRefreshDirectory()
                }
            }

            MainMultipleMenuEnum.Archive -> doEntryPackMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles
            )
        }
    }

    /**
     * 切换入文件粘贴模式
     */
    private suspend fun doEntryPasteMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
        isMoving: Boolean = false
    ) {
        if (sourceFiles.isEmpty()) {
            val message = get<Context>().getString(R.string.entry_paste_is_empty_message)
            AppViewEvent.PopupToastMessage(message).emit()
            return
        }
        getOrNull<MainUiState.Normal>()?.copy(
            viewModeState = MainListViewModeState.Paste(
                sourceStorageData = sourceStorageData,
                sourceDirectoryPath = sourceDirectoryPath,
                sourceFiles = sourceFiles,
                isMoving = isMoving
            )
        )?.setup()
    }

    /**
     * 切换入打包模式
     */
    private suspend fun doEntryPackMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
    ) {
        if (sourceFiles.isEmpty()) {
            val message = get<Context>().getString(R.string.entry_pack_is_empty_message)
            AppViewEvent.PopupToastMessage(message).emit()
            return
        }
        getOrNull<MainUiState.Normal>()?.copy(
            viewModeState = MainListViewModeState.Pack(
                sourceStorageData = sourceStorageData,
                sourceDirectoryPath = sourceDirectoryPath,
                sourceFiles = sourceFiles
            )
        )?.setup()
    }

    /**
     * 加载挂载的存储设备
     */
    private suspend fun loadExternalStorages(
        viewMode: MainListViewModeState = MainListViewModeState.Normal
    ) {
        val uiStatePrototype = MainUiState.Normal(viewModeState = viewMode)
        uiStatePrototype.copy(loadState = MainLoadState.ExternalStoragesLoading).setup()
        runCatching {
            withContext(Dispatchers.IO) { get<FileManager>().getMountedStorageVolumes() }
        }.onSuccess { storages ->
            uiStatePrototype.copy(
                listState = MainListState.StorageVolume(storageVolumes = storages)
            ).setup()
        }.onFailure { exception ->
            val message = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            AppViewEvent.PopupToastMessage(message).emit()
        }
    }

    /**
     * 加载目录信息
     */
    private suspend fun doLoadDirectory(
        storageData: StorageData,
        directoryPath: Path,
        viewModeState: MainListViewModeState = MainListViewModeState.Normal,
    ) {
        val uiStatePrototype = getOrNull<MainUiState.Normal>()?.copy(
            viewModeState = viewModeState
        ) ?: MainUiState.Normal(
            viewModeState = viewModeState
        )
        uiStatePrototype.copy(loadState = MainLoadState.DirectoryLoading).setup()
        val directory = File(directoryPath.toString())
        runCatching {
            if (!directory.canRead()) return@runCatching emptyList()
            withContext(Dispatchers.IO) {
                File(directoryPath.toString()).listFilteredFiles()
            }
        }.onSuccess { files ->
            uiStatePrototype.copy(
                listState = MainListState.Directory(
                    storageData = storageData,
                    directoryPath = directoryPath,
                    files = files,
                    canRead = directory.canRead(),
                    canWrite = directory.canWrite()
                )
            ).setup()
        }.onFailure { exception ->
            val message = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            AppViewEvent.PopupToastMessage(message).emit()
        }
    }

    /**
     * 执行删除文件操作
     */
    private suspend fun doDeleteFiles(fileSet: Set<File>): Boolean {
        if (fileSet.isEmpty()) {
            val message = get<Context>().getString(R.string.entry_paste_is_empty_message)
            AppViewEvent.PopupToastMessage(message).emit()
            return false
        }
        // 询问用户是否确认删除
        val isAgree = MainDialogState.FileDeleteConfirm(fileSet).run {
            popupAwaitDialogResult { deferredResult.awaitCompleted() }
        } == true
        if (!isAgree) return false
        // 执行文件删除逻辑
        fileSet.toList().deletes(onStartDelete = {
            getOrNull<MainUiState.Normal>()?.copy(
                loadState = MainLoadState.FilesDeleting(it)
            )?.setup()
        })
        // 重置页面当前loading状态为空
        awaitStateOf<MainUiState.Normal>().copy(
            loadState = MainLoadState.None
        ).setup()
        return true
    }

    /**
     * 粘贴模式底部菜单被点击
     */
    @UiIntentObserver(MainUiIntent.PasteMenuClick::class)
    private suspend fun onPasteMenuClick(intent: MainUiIntent.PasteMenuClick) {
        val state = getOrNull<MainUiState.Normal>() ?: return
        val viewMode = state.viewModeState as? MainListViewModeState.Paste ?: return
        when (intent.menu) {
            MainPasteMenuEnum.Paste -> doPasteFiles(
                targetStorageData = intent.targetStorageData,
                targetDirectoryPath = intent.targetDirectoryPath
            )

            else -> doLoadDirectory(
                storageData = viewMode.sourceStorageData,
                directoryPath = viewMode.sourceDirectoryPath
            )
        }
    }

    /**
     * 粘贴文件
     */
    private suspend fun doPasteFiles(
        targetStorageData: StorageData,
        targetDirectoryPath: Path,
    ) {
        var state = getOrNull<MainUiState.Normal>() ?: return
        val viewMode = state.viewModeState as? MainListViewModeState.Paste ?: return
        val targetDirectoryFile = File(targetDirectoryPath.toString())

        // 粘贴失败的列表
        val failureList = mutableListOf<File>()

        // 整体数量与已经完成的数量
        val totality = viewMode.sourceFiles.size
        var quantityCompleted = 0
        var fileConflictStrategy: FileConflictStrategy? = null

        // 逐个粘贴文件
        withContext(Dispatchers.IO) {
            viewMode.sourceFiles.forEach {
                val dest = File(targetDirectoryFile, it.name)
                // 更新加载状态
                state = MainLoadState
                    .Pasting(
                        isMoving = viewMode.isMoving,
                        src = it,
                        dest = dest,
                        totality = totality,
                        quantityCompleted = quantityCompleted
                    )
                    .run { state.copy(loadState = this) }
                    .apply { setup() }

                val isSuccessful = state.doPauseFile(
                    targetDirectoryFile = targetDirectoryFile,
                    src = it,
                    dest = dest,
                    isMoving = viewMode.isMoving,
                    fileConflictStrategy = fileConflictStrategy,
                    onUpdateFileConflictStrategy = { fileConflictStrategy = it }
                )
                if (!isSuccessful) {
                    failureList.add(it)
                }
                quantityCompleted += 1
            }
        }

        // 取消加载状态
        state.copy(loadState = MainLoadState.None).setup()

        doLoadDirectory(targetStorageData, targetDirectoryPath)
    }

    private suspend fun MainUiState.Normal.doPauseFile(
        targetDirectoryFile: File,
        src: File,
        dest: File,
        isMoving: Boolean,
        fileConflictStrategy: FileConflictStrategy?,
        onUpdateFileConflictStrategy: (FileConflictStrategy) -> Unit
    ): Boolean {
        var dest = dest
        var fileConflictStrategy = fileConflictStrategy
        if (dest.exists() && fileConflictStrategy == null) {
            val result = MainDialogState.FileConflict(oldFile = dest, newFile = src).run {
                popupAwaitDialogResult { deferredResult.awaitCompleted() }
            }
            fileConflictStrategy = result?.first ?: FileConflictStrategy.Skip
            if (result?.second == true) onUpdateFileConflictStrategy(fileConflictStrategy)
        }
        // 移动或拷贝文件
        if (dest.exists()) when (fileConflictStrategy) {
            null, FileConflictStrategy.Skip -> return true
            FileConflictStrategy.KeepBoth -> {
                dest = dest.generateUniqueFile(targetDirectoryFile)
            }

            FileConflictStrategy.Overwrite -> {
                dest.delete()
            }
        }

        return if (isMoving) {
            src.appMoveTo(dest)
        } else {
            src.appCopyTo(dest)
        }
    }

    /**
     * 打包菜单被选择
     */
    @UiIntentObserver(MainUiIntent.PackMenuClick::class)
    private suspend fun onPackMenuClick(
        intent: MainUiIntent.PackMenuClick
    ) {
        val state = getOrNull<MainUiState.Normal>() ?: return
        val viewMode = state.viewModeState as? MainListViewModeState.Pack ?: return
        when (intent.menu) {
            MainPackMenuEnum.Pack -> doPackFiles(
                targetStorageData = intent.targetStorageData,
                targetDirectoryPath = intent.targetDirectoryPath
            )

            MainPackMenuEnum.Cancel -> doLoadDirectory(
                storageData = viewMode.sourceStorageData,
                directoryPath = viewMode.sourceDirectoryPath
            )
        }
    }

    /**
     * 执行具体的打包操作
     */
    private suspend fun doPackFiles(
        targetStorageData: StorageData,
        targetDirectoryPath: Path = Path(""),
    ) {
        val state = getOrNull<MainUiState.Normal>() ?: return
        val viewMode = state.viewModeState as? MainListViewModeState.Pack ?: return

        val params = CreateArchiveActivity.params(
            dtm = mDataTransferManager,
            files = viewMode.sourceFiles.toList(),
            targetStorageData = targetStorageData,
            targetDirectoryPath = targetDirectoryPath
        )
        AppViewEvent.StartActivity(
            activity = CreateArchiveActivity::class.java,
            extras = params
        ).emit()
        doLoadDirectory(targetStorageData, targetDirectoryPath)
    }

    /**
     * 主页弹窗通用流程
     */
    private suspend fun <R> MainDialogState.popupAwaitDialogResult(
        onAwaitResult: suspend () -> R
    ): R? {
        val dialog = this
        val result = awaitStateOf<MainUiState.Normal>().run {
            copy(dialogStates = dialogStates.toMutableSet().apply { add(dialog) }).setup()
            onAwaitResult()
        }
        awaitStateOf<MainUiState.Normal>().run {
            copy(dialogStates = dialogStates.toMutableSet().apply { remove(dialog) }).setup()
        }
        return result
    }

    @UiIntentObserver(MainUiIntent.SelectAllClick::class)
    private fun onSelectAllClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        uiState.copy(
            viewModeState = viewModeState.copy(selected = listState.files.toSet())
        ).setup()
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
        if (uiState.loadState is MainLoadState.QueryDuplicateFiles) {
            cancelActiveTaskAndRestore()
        }
    }

    @UiIntentObserver(MainUiIntent.InvertSelectionClick::class)
    private fun onInvertSelectionClick() {
        val uiState = getOrNull<MainUiState.Normal>() ?: return
        val listState = uiState.listState as? MainListState.Directory ?: return
        val viewModeState = uiState.viewModeState as? MainListViewModeState.MultipleSelect ?: return
        val all = listState.files.toSet()
        val inverted = all - viewModeState.selected
        uiState.copy(
            viewModeState = viewModeState.copy(selected = inverted)
        ).setup()
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
            doRefreshDirectory()
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
            doRefreshDirectory()
        }
    }
}