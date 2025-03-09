package cc.kafuu.archandler.feature.main

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
import cc.kafuu.archandler.libs.core.CoreViewModel
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

class MainViewModel : CoreViewModel<MainUiIntent, MainUiState, MainSingleEvent>(), KoinComponent {
    override fun onCollectedIntent(uiIntent: MainUiIntent) {
        when (uiIntent) {
            MainUiIntent.Init -> initViewModel()

            MainUiIntent.JumpFilePermissionSetting -> dispatchingEvent(
                event = MainSingleEvent.JumpFilePermissionSetting
            )

            MainUiIntent.BackToNormalViewMode -> onBackToNormalViewMode()

            is MainUiIntent.MainDrawerMenuClick -> onProcessingIntent(uiIntent)
            is MainUiIntent.StorageVolumeSelected -> onProcessingIntent(uiIntent)
            is MainUiIntent.FileSelected -> onProcessingIntent(uiIntent)
            is MainUiIntent.BackToParent -> onProcessingIntent(uiIntent)
            is MainUiIntent.FileCheckedChange -> onProcessingIntent(uiIntent)
            is MainUiIntent.FileMultipleSelectMode -> onProcessingIntent(uiIntent)
            is MainUiIntent.MultipleMenuClick -> onProcessingIntent(uiIntent)
        }
    }

    private fun initViewModel() {
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.PermissionDenied.setup()
        } else {
            loadExternalStorages()
        }
    }

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

    private fun onProcessingIntent(intent: MainUiIntent.StorageVolumeSelected) {
        val state = (uiState.value as? MainUiState.Accessible) ?: return
        loadDirectory(
            storageData = intent.storageData,
            directoryPath = Path(intent.storageData.directory.path),
            viewModeState = state.viewModeState
        )
    }

    private fun onProcessingIntent(intent: MainUiIntent.FileSelected) {
        val state = (uiState.value as? MainUiState.Accessible) ?: return
        if (intent.file.isDirectory) {
            loadDirectory(
                storageData = intent.storageData,
                directoryPath = Path(intent.file.path),
                viewModeState = state.viewModeState
            )
            return
        }
    }

    private fun onProcessingIntent(intent: MainUiIntent.FileMultipleSelectMode) {
        (uiState.value as? MainUiState.Accessible)?.copy(
            viewModeState = if (intent.enable) {
                MainListViewModeState.MultipleSelect()
            } else {
                MainListViewModeState.Normal
            }
        )?.setup()
    }

    private fun onProcessingIntent(intent: MainUiIntent.MultipleMenuClick) {
        when (intent.menu) {
            MainMultipleMenuEnum.Copy -> entryPauseMode(
                sourceStorageData = intent.sourceStorageData,
                sourceDirectoryPath = intent.sourceDirectoryPath,
                sourceFiles = intent.sourceFiles,
                isMoving = false
            )

            MainMultipleMenuEnum.Move -> entryPauseMode(
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
    }

    private fun entryPauseMode(
        sourceStorageData: StorageData,
        sourceDirectoryPath: Path,
        sourceFiles: List<File>,
        isMoving: Boolean = false
    ) {
        (uiState.value as? MainUiState.Accessible)?.copy(
            viewModeState = MainListViewModeState.Pause(
                sourceStorageData = sourceStorageData,
                sourceDirectoryPath = sourceDirectoryPath,
                sourceFiles = sourceFiles,
                isMoving = isMoving
            )
        )?.setup()
    }

    private fun onProcessingIntent(intent: MainUiIntent.FileCheckedChange) {
        val state = uiState.value as? MainUiState.Accessible ?: return
        (state.viewModeState as? MainListViewModeState.MultipleSelect)?.let {
            val selected = it.selected.toMutableSet().apply {
                if (intent.checked) add(intent.file) else remove(intent.file)
            }
            state.copy(viewModeState = MainListViewModeState.MultipleSelect(selected = selected))
        }?.setup()
    }

    private fun onProcessingIntent(
        intent: MainUiIntent.BackToParent
    ) {
        val state = (uiState.value as? MainUiState.Accessible) ?: return

        val parent = intent.currentPath.getParentPath()
        if (parent == null ||
            Path(intent.storageData.directory.path).isSameFileOrDirectory(intent.currentPath)
        ) {
            loadExternalStorages(state.viewModeState)
        } else {
            loadDirectory(
                storageData = intent.storageData,
                directoryPath = parent,
                viewModeState = state.viewModeState
            )
        }
    }

    private fun onBackToNormalViewMode() {
        val state = (uiState.value as? MainUiState.Accessible) ?: return
        when (val viewMode = state.viewModeState) {
            is MainListViewModeState.Normal, is MainListViewModeState.MultipleSelect -> {
                (uiState.value as? MainUiState.Accessible)?.copy(
                    viewModeState = MainListViewModeState.Normal
                )?.setup()
            }

            is MainListViewModeState.Pause -> loadDirectory(
                storageData = viewMode.sourceStorageData,
                directoryPath = viewMode.sourceDirectoryPath
            )
        }
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

    private fun loadDirectory(
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