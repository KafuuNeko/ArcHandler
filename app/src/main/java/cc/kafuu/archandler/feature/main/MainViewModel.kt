package cc.kafuu.archandler.feature.main

import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainListData
import cc.kafuu.archandler.feature.main.presentation.MainListViewMode
import cc.kafuu.archandler.feature.main.presentation.MainSingleEvent
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreViewModel
import cc.kafuu.archandler.libs.ext.castOrNull
import cc.kafuu.archandler.libs.ext.getParentPath
import cc.kafuu.archandler.libs.ext.isSameFileOrDirectory
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.LoadingState
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

            is MainUiIntent.MainDrawerMenuClick -> onMainDrawerMenuClick(uiIntent.menu)

            is MainUiIntent.StorageVolumeSelected -> onStorageVolumeSelected(
                storageData = uiIntent.storageData
            )

            is MainUiIntent.FileSelected -> onFileSelected(
                storageData = uiIntent.storageData,
                file = uiIntent.file
            )

            is MainUiIntent.BackToParent -> onBackToParent(
                storageData = uiIntent.storageData,
                currentPath = uiIntent.currentPath
            )

            is MainUiIntent.FileCheckedChange -> onFileCheckedChange(
                file = uiIntent.file,
                checked = uiIntent.checked
            )

            is MainUiIntent.FileMultipleSelectMode -> onFileMultipleSelectModeChange(
                enable = uiIntent.enable
            )

            is MainUiIntent.MultipleMenuClick -> {
                // TODO: Waiting for implementation
            }

            MainUiIntent.BackToNormalViewMode -> onBackToNormalViewMode()
        }
    }

    private fun initViewModel() {
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.PermissionDenied.setup()
        } else {
            loadExternalStorages()
        }
    }

    private fun onMainDrawerMenuClick(
        menu: MainDrawerMenuEnum
    ) = when (menu) {
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
            // TODO: 待实现关于页
        }
    }

    private fun onStorageVolumeSelected(
        storageData: StorageData
    ) = loadDirectory(
        storageData = storageData,
        directoryPath = Path(storageData.directory.path)
    )

    private fun onFileSelected(
        storageData: StorageData,
        file: File
    ) {
        if (file.isDirectory) {
            loadDirectory(
                storageData = storageData,
                directoryPath = Path(file.path)
            )
            return
        }
    }

    private fun onFileMultipleSelectModeChange(
        enable: Boolean
    ) {
        uiState.value?.castOrNull<MainUiState.Accessible>()?.copy(
            viewMode = if (enable) {
                MainListViewMode.MultipleSelect()
            } else {
                MainListViewMode.Normal
            }
        )?.setup()
    }

    private fun onFileCheckedChange(
        file: File,
        checked: Boolean
    ) {
        val state = uiState.value?.castOrNull<MainUiState.Accessible>() ?: return
        state.viewMode.castOrNull<MainListViewMode.MultipleSelect>()?.let {
            val selected = it.selected.toMutableSet().apply {
                if (checked) add(file) else remove(file)
            }
            state.copy(viewMode = MainListViewMode.MultipleSelect(selected = selected))
        }?.setup()
    }

    private fun onBackToParent(
        storageData: StorageData,
        currentPath: Path
    ) {
        val state = uiState.value?.castOrNull<MainUiState.Accessible>() ?: return

        val parent = currentPath.getParentPath()
        if (parent == null || Path(storageData.directory.path).isSameFileOrDirectory(currentPath)) {
            loadExternalStorages(state.viewMode)
        } else {
            loadDirectory(
                storageData = storageData,
                directoryPath = parent
            )
        }
    }

    private fun onBackToNormalViewMode() {
        val state = uiState.value?.castOrNull<MainUiState.Accessible>() ?: return
        when (val viewMode = state.viewMode) {
            is MainListViewMode.Normal, is MainListViewMode.MultipleSelect -> {
                uiState.value?.castOrNull<MainUiState.Accessible>()?.copy(
                    viewMode = MainListViewMode.Normal
                )?.setup()
            }

            is MainListViewMode.Pause -> loadDirectory(
                storageData = viewMode.sourceStorageData,
                directoryPath = viewMode.sourceDirectoryPath
            )
        }
    }

    private fun loadExternalStorages(
        viewMode: MainListViewMode = MainListViewMode.Normal
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.Accessible(viewMode = viewMode)
        uiStatePrototype.copy(loadingState = LoadingState(isLoading = true)).setup()
        runCatching {
            get<FileManager>().getMountedStorageVolumes()
        }.onSuccess { storages ->
            uiStatePrototype.copy(
                listData = MainListData.StorageVolume(storageVolumes = storages)
            ).setup()
        }.onFailure { exception ->
            uiStatePrototype.copy(
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }

    private fun loadDirectory(
        storageData: StorageData,
        directoryPath: Path
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.Accessible()
        uiStatePrototype.copy(loadingState = LoadingState(isLoading = true)).setup()
        runCatching {
            File(directoryPath.toString()).listFiles()?.asList() ?: emptyList()
        }.onSuccess { files ->
            uiStatePrototype.copy(
                listData = MainListData.Directory(
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