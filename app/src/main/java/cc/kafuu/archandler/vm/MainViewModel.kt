package cc.kafuu.archandler.vm

import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreViewModel
import cc.kafuu.archandler.libs.model.StorageData
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.nio.file.Path

class MainViewModel : CoreViewModel<MainUiIntent, MainUiState, MainSingleEvent>(), KoinComponent {
    override fun onCollectedIntent(uiIntent: MainUiIntent) {
        when (uiIntent) {
            MainUiIntent.Init -> initViewModel()

            MainUiIntent.JumpFilePermissionSetting -> dispatchingEvent(
                MainSingleEvent.JumpFilePermissionSetting
            )

            is MainUiIntent.ChangeDirectory -> {
                // TODO: Waiting for implementation
            }

            MainUiIntent.AboutClick -> {
                // TODO: Waiting for implementation
            }

            MainUiIntent.CodeRepositoryClick -> {
                get<AppLibs>().jumpToUrl(AppModel.CODE_REPOSITORY_URL)
            }

            MainUiIntent.FeedbackClick -> {
                get<AppLibs>().jumpToUrl(AppModel.FEEDBACK_URL)
            }

            MainUiIntent.RateClick -> {
                get<AppLibs>().jumpToUrl(AppModel.GOOGLE_PLAY_URL)
            }
        }
    }

    private fun initViewModel() {
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            MainUiState.NotPermission.setup()
        } else {
            loadExternalStorages()
        }
    }

    private fun loadExternalStorages() = viewModelScope.launch(Dispatchers.IO) {
        MainUiState.StorageVolumeList(loading = true).setup()
        runCatching {
            get<AppLibs>().getMountedStorageVolumes()
        }.onSuccess { storages ->
            MainUiState.StorageVolumeList(loading = false, storageVolumes = storages).setup()
        }.onFailure { exception ->
            MainUiState.StorageVolumeList(
                loading = false,
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }

    private fun loadDirectory(
        storageData: StorageData,
        directoryPath: Path
    ) = viewModelScope.launch(Dispatchers.IO) {
        MainUiState.DirectoryList(
            loading = true,
            storageData = storageData,
            directoryPath = directoryPath
        ).setup()
        runCatching {
            File(directoryPath.toString()).listFiles()?.asList() ?: emptyList()
        }.onSuccess { files ->
            MainUiState.DirectoryList(
                loading = false,
                storageData = storageData,
                directoryPath = directoryPath,
                files = files
            ).setup()
        }.onFailure { exception ->
            MainUiState.DirectoryList(
                loading = false,
                storageData = storageData,
                directoryPath = directoryPath,
                errorMessage = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).setup()
        }
    }
}

sealed class MainUiIntent {
    data object Init : MainUiIntent()
    data object JumpFilePermissionSetting : MainUiIntent()
    data class ChangeDirectory(
        val directoryPath: Path
    ) : MainUiIntent()

    data object CodeRepositoryClick : MainUiIntent()
    data object FeedbackClick : MainUiIntent()
    data object RateClick : MainUiIntent()
    data object AboutClick : MainUiIntent()
}

sealed class MainUiState {
    // User has not authorized 'MANAGE_EXTERNAL_STORAGE'
    data object NotPermission : MainUiState()

    data class StorageVolumeList(
        val loading: Boolean,
        val errorMessage: String? = null,
        val storageVolumes: List<StorageData> = emptyList()
    ) : MainUiState()

    data class DirectoryList(
        val loading: Boolean,
        val errorMessage: String? = null,
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
    ) : MainUiState()
}

sealed class MainSingleEvent {
    data object JumpFilePermissionSetting : MainSingleEvent()
}