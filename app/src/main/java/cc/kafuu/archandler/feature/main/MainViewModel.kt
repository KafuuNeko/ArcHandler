package cc.kafuu.archandler.feature.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
import cc.kafuu.archandler.feature.main.presentation.LoadingState
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainSingleEvent
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.ext.getParentPath
import cc.kafuu.archandler.libs.ext.isSameFileOrDirectory
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.StorageData
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

class MainViewModel : CoreViewModelWithEvent<MainUiIntent, MainUiState, MainSingleEvent>(
    initStatus = MainUiState.None
), KoinComponent {
    @UiIntentObserver(MainUiIntent.Init::class)
    private fun onInit() {
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.PermissionDenied.setup()
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
        if (state.loadingState.isLoading) return

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

            is MainListViewModeState.Pause -> {
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
        dispatchingEvent(MainSingleEvent.JumpFilePermissionSetting)
    }

    /**
     * 处理主页抽屉按钮点击事件
     */
    @UiIntentObserver(MainUiIntent.MainDrawerMenuClick::class)
    private fun onProcessingIntent(intent: MainUiIntent.MainDrawerMenuClick) {
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
                dispatchingEvent(event = MainSingleEvent.JumpAboutPage)
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
    private fun onProcessingIntent(intent: MainUiIntent.FileSelected) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
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
    }

    /**
     * 切换用户多选模式
     */
    @UiIntentObserver(MainUiIntent.FileMultipleSelectMode::class)
    private fun onProcessingIntent(intent: MainUiIntent.FileMultipleSelectMode) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
        when (state.viewModeState) {
            is MainListViewModeState.Pause -> return
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
    ) = when (intent.menu) {
        MainMultipleMenuEnum.Copy -> doEntryPauseMode(
            sourceStorageData = intent.sourceStorageData,
            sourceDirectoryPath = intent.sourceDirectoryPath,
            sourceFiles = intent.sourceFiles,
            isMoving = false
        )

        MainMultipleMenuEnum.Move -> doEntryPauseMode(
            sourceStorageData = intent.sourceStorageData,
            sourceDirectoryPath = intent.sourceDirectoryPath,
            sourceFiles = intent.sourceFiles,
            isMoving = true
        )

        MainMultipleMenuEnum.Delete -> {
            // TODO:
        }

        MainMultipleMenuEnum.Archive -> {
            // TODO:
        }
    }

    /**
     * 切换入文件粘贴模式
     */
    private fun doEntryPauseMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
        isMoving: Boolean = false
    ) {
        val state = fetchUiState() as? MainUiState.Accessible ?: return
        if (sourceFiles.isEmpty()) {
            val message = get<Context>().getString(R.string.entry_pause_is_empty_message)
            dispatchingEvent(MainSingleEvent.PopupToastMessage(message))
            return
        }
        state.copy(
            viewModeState = MainListViewModeState.Pause(
                sourceStorageData = sourceStorageData,
                sourceDirectoryPath = sourceDirectoryPath,
                sourceFiles = sourceFiles,
                isMoving = isMoving
            )
        ).setup()
    }

    private fun loadExternalStorages(
        viewMode: MainListViewModeState = MainListViewModeState.Normal
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.Accessible(viewModeState = viewMode)
        uiStatePrototype.copy(loadingState = LoadingState(isLoading = true)).setup()
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

    private fun doLoadDirectory(
        storageData: StorageData,
        directoryPath: Path,
        viewModeState: MainListViewModeState = MainListViewModeState.Normal,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.Accessible(
            viewModeState = viewModeState
        )
        uiStatePrototype.copy(loadingState = LoadingState(isLoading = true)).setup()
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
}