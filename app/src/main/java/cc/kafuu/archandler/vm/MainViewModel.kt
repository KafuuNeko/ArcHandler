package cc.kafuu.archandler.vm

import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
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

            MainUiIntent.AboutClick -> {
                // TODO: Waiting for implementation
            }

            MainUiIntent.CodeRepositoryClick -> get<AppLibs>().jumpToUrl(AppModel.CODE_REPOSITORY_URL)

            MainUiIntent.FeedbackClick -> get<AppLibs>().jumpToUrl(AppModel.FEEDBACK_URL)

            MainUiIntent.RateClick -> get<AppLibs>().jumpToUrl(AppModel.GOOGLE_PLAY_URL)

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
        }
    }

    private fun initViewModel() {
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.PermissionDenied.setup()
        } else {
            loadExternalStorages()
        }
    }

    private fun onStorageVolumeSelected(
        storageData: StorageData
    ) {
        loadDirectory(
            storageData = storageData,
            directoryPath = Path(storageData.directory.path)
        )
    }

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
        uiState.value?.castOrNull<MainUiState.DirectoryList>()?.copy(
            viewMode = if (enable) {
                MainDirectoryViewMode.MultipleSelect()
            } else {
                MainDirectoryViewMode.Normal
            }
        )?.setup()
    }

    private fun onFileCheckedChange(
        file: File,
        checked: Boolean
    ) {
        val state = uiState.value?.castOrNull<MainUiState.DirectoryList>() ?: return
        state.viewMode.castOrNull<MainDirectoryViewMode.MultipleSelect>()?.let {
            val selected = it.selected.toMutableSet().apply {
                if (checked) add(file) else remove(file)
            }
            state.copy(viewMode = MainDirectoryViewMode.MultipleSelect(selected = selected))
        }?.setup()
    }

    private fun onBackToParent(
        storageData: StorageData,
        currentPath: Path
    ) {
        val parent = currentPath.getParentPath()
        if (parent == null || Path(storageData.directory.path).isSameFileOrDirectory(currentPath)) {
            loadExternalStorages()
        } else {
            loadDirectory(
                storageData = storageData,
                directoryPath = parent
            )
        }
    }

    private fun loadExternalStorages() = viewModelScope.launch(Dispatchers.IO) {
        MainUiState.StorageVolumeList(loadingState = LoadingState(isLoading = true)).setup()
        runCatching {
            get<FileManager>().getMountedStorageVolumes()
        }.onSuccess { storages ->
            MainUiState.StorageVolumeList(storageVolumes = storages).setup()
        }.onFailure { exception ->
            MainUiState.StorageVolumeList(
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }

    private fun loadDirectory(
        storageData: StorageData,
        directoryPath: Path
    ) = viewModelScope.launch(Dispatchers.IO) {
        val uiStatePrototype = MainUiState.DirectoryList(
            storageData = storageData,
            directoryPath = directoryPath
        )
        uiStatePrototype.copy(loadingState = LoadingState(isLoading = true)).setup()
        runCatching {
            File(directoryPath.toString()).listFiles()?.asList() ?: emptyList()
        }.onSuccess { files ->
            uiStatePrototype.copy(files = files).setup()
        }.onFailure { exception ->
            uiStatePrototype.copy(
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }
}

sealed class MainUiIntent {
    data object Init : MainUiIntent()
    data object JumpFilePermissionSetting : MainUiIntent()

    data object CodeRepositoryClick : MainUiIntent()
    data object FeedbackClick : MainUiIntent()
    data object RateClick : MainUiIntent()
    data object AboutClick : MainUiIntent()

    data class StorageVolumeSelected(
        val storageData: StorageData
    ) : MainUiIntent()

    data class FileSelected(
        val storageData: StorageData,
        val file: File
    ) : MainUiIntent()

    data class FileMultipleSelectMode(
        val enable: Boolean
    ) : MainUiIntent()

    data class FileCheckedChange(
        val file: File,
        val checked: Boolean
    ) : MainUiIntent()

    data class BackToParent(
        val storageData: StorageData,
        val currentPath: Path
    ) : MainUiIntent()
}

sealed class MainUiState(
    open val loadingState: LoadingState = LoadingState()
) {
    data object PermissionDenied : MainUiState()

    data class StorageVolumeList(
        override val loadingState: LoadingState = LoadingState(),
        val errorMessage: String? = null,
        val storageVolumes: List<StorageData> = emptyList()
    ) : MainUiState(loadingState = loadingState)

    data class DirectoryList(
        override val loadingState: LoadingState = LoadingState(),
        val errorMessage: String? = null,
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
        val viewMode: MainDirectoryViewMode = MainDirectoryViewMode.Normal
    ) : MainUiState(loadingState = loadingState)
}

sealed class MainDirectoryViewMode {
    data object Normal : MainDirectoryViewMode()

    data class MultipleSelect(
        val selected: Set<File> = emptySet()
    ) : MainDirectoryViewMode()

    data class Pause(
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
        val isMoving: Boolean = false
    ) : MainDirectoryViewMode()
}

sealed class MainSingleEvent {
    data object JumpFilePermissionSetting : MainSingleEvent()
}