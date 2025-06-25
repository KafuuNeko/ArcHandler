package cc.kafuu.archandler.feature.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
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
import cc.kafuu.archandler.libs.core.CoreViewModel
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.core.toViewEvent
import cc.kafuu.archandler.libs.ext.appCopyTo
import cc.kafuu.archandler.libs.ext.appMoveTo
import cc.kafuu.archandler.libs.ext.createUniqueDirectory
import cc.kafuu.archandler.libs.ext.deletes
import cc.kafuu.archandler.libs.ext.getParentPath
import cc.kafuu.archandler.libs.ext.getSameNameDirectory
import cc.kafuu.archandler.libs.ext.isSameFileOrDirectory
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.StorageData
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

class MainViewModel : CoreViewModel<MainUiIntent, MainUiState>(
    initStatus = MainUiState.None
), KoinComponent {
    // 应用通用工具
    private val mAppLibs by inject<AppLibs>()

    // 压缩包管理器
    private val mArchiveManager by inject<ArchiveManager>()

    // 压缩包密码提供请求接口
    val mPasswordProvider = object : IPasswordProvider {
        override suspend fun getPassword(
            file: File
        ): String? = MainDialogState.PasswordInput(file = file).run {
            popupAwaitDialogResult { resultFuture.awaitResult() }
        }
    }

    /**
     * 页面初始化
     */
    @UiIntentObserver(MainUiIntent.Init::class)
    private fun onInit() {
        // 只有在空页或者未授权状态下才可继续执行
        when (fetchUiState()) {
            MainUiState.None, is MainUiState.PermissionDenied -> Unit
            else -> return
        }
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.PermissionDenied().setup()
        } else {
            loadExternalStorages()
        }
    }

    /**
     * 页面返回逻辑
     */
    @UiIntentObserver(MainUiIntent.Back::class)
    private fun onBack() {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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
        }
    }

    /**
     * 返回到上一级目录
     */
    private fun doBackToParent(
        storageData: StorageData,
        currentPath: Path
    ) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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
    private fun onJumpFilePermissionSetting() {
        val state = fetchUiState() as? MainUiState.PermissionDenied ?: return
        state.copy(viewEvent = MainViewEvent.JumpFilePermissionSetting.toViewEvent()).setup()
    }

    /**
     * 处理主页抽屉按钮点击事件
     */
    @UiIntentObserver(MainUiIntent.MainDrawerMenuClick::class)
    private fun onProcessingIntent(intent: MainUiIntent.MainDrawerMenuClick) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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

            MainDrawerMenuEnum.About -> {
                state.copy(viewEvent = MainViewEvent.JumpAboutPage.toViewEvent()).setup()
            }
        }
    }

    /**
     * 用户选择存储设备
     */
    @UiIntentObserver(MainUiIntent.StorageVolumeSelected::class)
    private fun onProcessingIntent(intent: MainUiIntent.StorageVolumeSelected) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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
    private fun onProcessingIntent(intent: MainUiIntent.FileSelected) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return@launch
        val listState = state.listState as? MainListState.Directory ?: return@launch

        // 文件多选模式用户选择行为为切换选中模式
        if (state.viewModeState is MainListViewModeState.MultipleSelect) {
            val viewMode = state.viewModeState
            val selected = viewMode.selected.toMutableSet().apply {
                if (viewMode.selected.contains(intent.file)) remove(intent.file) else add(intent.file)
            }
            state.copy(viewModeState = viewMode.copy(selected = selected)).setup()
            return@launch
        }
        // 如果用户选择的是目录则切换进目录
        if (intent.file.isDirectory) {
            doLoadDirectory(
                storageData = intent.storageData,
                directoryPath = Path(intent.file.path),
                viewModeState = state.viewModeState
            )
            return@launch
        }
        // 判断当前打开的文件是否可解压
        if (!mArchiveManager.isExtractable(intent.file)) {
            state.copy(viewEvent = MainViewEvent.OpenFile(intent.file).toViewEvent()).setup()
            return@launch
        }
        // 开始解压压缩包
        val destDir = doDecompress(intent.file) ?: run {
            // 解压失败
            state.copy(
                viewEvent = MainViewEvent.PopupToastMessage(mAppLibs.getString(R.string.archive_unpacking_failed_message))
                    .toViewEvent(),
                loadState = MainLoadState.None
            ).setup()
            return@launch
        }
        // 解压成功，进入解压后的目录
        doLoadDirectory(listState.storageData, Path(destDir.path))
    }

    /**
     * 执行压缩包解压流程
     */
    private suspend fun doDecompress(file: File): File? = runCatching {
        val state = fetchUiState() as? MainUiState.Accessible ?: return null

        // 尝试打开压缩包
        val archive = mArchiveManager.openArchive(file) ?: return null
        if (!archive.open(mPasswordProvider)) return null
        // 提取压缩包文件
        archive.use { archive ->
            val destDir = file.getSameNameDirectory().createUniqueDirectory() ?: return@use null
            archive.extractAll(destDir) { index, path, target ->
                state.copy(loadState = MainLoadState.Unpacking(file, index, path, target)).setup()
            }
            return@use destDir
        }
    }.getOrNull()

    /**
     * 切换用户多选模式
     */
    @UiIntentObserver(MainUiIntent.FileMultipleSelectMode::class)
    private fun onProcessingIntent(intent: MainUiIntent.FileMultipleSelectMode) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
        when (state.viewModeState) {
            is MainListViewModeState.Paste -> return
            else -> Unit
        }
        state.copy(
            viewModeState = if (intent.enable) {
                MainListViewModeState.MultipleSelect()
            } else {
                MainListViewModeState.Normal
            }
        ).setup()
    }

    /**
     * 文件多选模式底部菜单点击事件
     */
    @UiIntentObserver(MainUiIntent.MultipleMenuClick::class)
    private fun onProcessingIntent(
        intent: MainUiIntent.MultipleMenuClick
    ) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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

            MainMultipleMenuEnum.Delete -> viewModelScope.launch(Dispatchers.IO) {
                if (doDeleteFiles(viewModel.selected)) {
                    doLoadDirectory(listState.storageData, listState.directoryPath)
                }
            }

            MainMultipleMenuEnum.Archive -> {
                // TODO:
            }
        }
    }

    /**
     * 切换入文件粘贴模式
     */
    private fun doEntryPasteMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
        isMoving: Boolean = false
    ) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
        if (sourceFiles.isEmpty()) {
            val message = get<Context>().getString(R.string.entry_paste_is_empty_message)
            state.copy(viewEvent = MainViewEvent.PopupToastMessage(message).toViewEvent()).setup()
            return
        }
        state.copy(
            viewModeState = MainListViewModeState.Paste(
                sourceStorageData = sourceStorageData,
                sourceDirectoryPath = sourceDirectoryPath,
                sourceFiles = sourceFiles,
                isMoving = isMoving
            )
        ).setup()
    }

    /**
     * 加载挂载的存储设备
     */
    private fun loadExternalStorages(
        viewMode: MainListViewModeState = MainListViewModeState.Normal
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.Accessible(viewModeState = viewMode)
        uiStatePrototype.copy(loadState = MainLoadState.ExternalStoragesLoading).setup()
        runCatching {
            get<FileManager>().getMountedStorageVolumes()
        }.onSuccess { storages ->
            uiStatePrototype.copy(
                listState = MainListState.StorageVolume(storageVolumes = storages)
            ).setup()
        }.onFailure { exception ->
            uiStatePrototype.copy(
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }

    /**
     * 加载目录信息
     */
    private fun doLoadDirectory(
        storageData: StorageData,
        directoryPath: Path,
        viewModeState: MainListViewModeState = MainListViewModeState.Normal,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.Accessible(
            viewModeState = viewModeState
        )
        uiStatePrototype.copy(loadState = MainLoadState.DirectoryLoading).setup()
        runCatching {
            File(directoryPath.toString()).listFiles()?.asList() ?: emptyList()
        }.onSuccess { files ->
            uiStatePrototype.copy(
                listState = MainListState.Directory(
                    storageData = storageData,
                    directoryPath = directoryPath,
                    files = files
                )
            ).setup()
        }.onFailure { exception ->
            uiStatePrototype.copy(
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }

    /**
     * 执行删除文件操作
     */
    private suspend fun doDeleteFiles(fileSet: Set<File>): Boolean {
        // 询问用户是否确认删除
        val isAgree = MainDialogState.FileDeleteConfirm(fileSet).run {
            popupAwaitDialogResult { resultFuture.awaitResult() }
        } == true
        if (!isAgree) return false
        // 执行文件删除逻辑
        fileSet.toList().deletes(onStartDelete = {
            (fetchUiState() as? MainUiState.Accessible)?.copy(
                loadState = MainLoadState.FilesDeleting(it)
            )?.setup()
        })
        // 重置页面当前loading状态为空
        awaitUiStateOfType<MainUiState.Accessible>().copy(
            loadState = MainLoadState.None
        ).setup()
        return true
    }

    /**
     * 粘贴模式底部菜单被点击
     */
    @UiIntentObserver(MainUiIntent.PasteMenuClick::class)
    private fun onPasteMenuClick(intent: MainUiIntent.PasteMenuClick) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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
    private fun doPasteFiles(
        targetStorageData: StorageData,
        targetDirectoryPath: Path,
    ) = viewModelScope.launch {
        val state = fetchUiState() as? MainUiState.Accessible ?: return@launch
        val viewMode = state.viewModeState as? MainListViewModeState.Paste ?: return@launch
        val targetDirectoryFile = File(targetDirectoryPath.toString())

        // 粘贴失败的列表
        val failureList = mutableListOf<File>()

        // 整体数量与已经完成的数量
        val totality = viewMode.sourceFiles.size
        var quantityCompleted = 0

        // 逐个粘贴文件
        viewMode.sourceFiles.forEach {
            val dest = File(targetDirectoryFile, it.name)
            // 更新加载状态
            MainLoadState.Pasting(
                isMoving = viewMode.isMoving,
                src = it,
                dest = dest,
                totality = totality,
                quantityCompleted = quantityCompleted
            ).run { state.copy(loadState = this).setup() }
            // 移动或拷贝文件
            val isSuccess = if (dest.exists()) {
                false
            } else if (viewMode.isMoving) {
                it.appMoveTo(dest)
            } else {
                it.appCopyTo(dest)
            }
            if (!isSuccess) failureList.add(it)
            quantityCompleted += 1
        }

        // 取消加载状态
        state.copy(loadState = MainLoadState.None).setup()

        doLoadDirectory(targetStorageData, targetDirectoryPath)
    }

    /**
     * 主页弹窗通用流程
     */
    private suspend fun <R> MainDialogState.popupAwaitDialogResult(
        onAwaitResult: suspend () -> Result<R>
    ): R? {
        val dialog = this
        val result = awaitUiStateOfType<MainUiState.Accessible>().run {
            copy(dialogStates = dialogStates.toMutableSet().apply { add(dialog) }).setup()
            onAwaitResult()
        }
        awaitUiStateOfType<MainUiState.Accessible>().run {
            copy(dialogStates = dialogStates.toMutableSet().apply { remove(dialog) }).setup()
        }
        return result.getOrNull()
    }
}