package cc.kafuu.archandler.vm

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
        menu: MainDrawerMenu
    ) = when (menu) {
        MainDrawerMenu.Code -> {
            get<AppLibs>().jumpToUrl(AppModel.CODE_REPOSITORY_URL)
        }

        MainDrawerMenu.Feedback -> {
            get<AppLibs>().jumpToUrl(AppModel.FEEDBACK_URL)
        }

        MainDrawerMenu.Rate -> {
            get<AppLibs>().jumpToUrl(AppModel.GOOGLE_PLAY_URL)
        }

        MainDrawerMenu.About -> {
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

sealed class MainUiIntent {
    data object Init : MainUiIntent()
    data object JumpFilePermissionSetting : MainUiIntent()

    data class MainDrawerMenuClick(
        val menu: MainDrawerMenu
    ) : MainUiIntent()

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

    data object BackToNormalViewMode : MainUiIntent()

    data class MultipleMenuClick(
        val menu: MainMultipleMenu,
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
    ) : MainUiIntent()
}

sealed class MainUiState(
    open val loadingState: LoadingState = LoadingState()
) {
    data object PermissionDenied : MainUiState()

    data class Accessible(
        override val loadingState: LoadingState = LoadingState(),
        val errorMessage: String? = null,
        val viewMode: MainListViewMode = MainListViewMode.Normal,
        val listData: MainListData = MainListData.Undecided,
    ) : MainUiState(loadingState = loadingState)
}

sealed class MainListData {
    data object Undecided : MainListData()

    data class StorageVolume(
        val storageVolumes: List<StorageData> = emptyList()
    ) : MainListData()

    data class Directory(
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
    ) : MainListData()
}

sealed class MainListViewMode {
    data object Normal : MainListViewMode()

    data class MultipleSelect(
        val selected: Set<File> = emptySet()
    ) : MainListViewMode()

    data class Pause(
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
        val isMoving: Boolean = false
    ) : MainListViewMode()
}

sealed class MainSingleEvent {
    data object JumpFilePermissionSetting : MainSingleEvent()
}

enum class MainDrawerMenu(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    Code(R.drawable.ic_code, R.string.code_repository),
    Feedback(R.drawable.ic_feedback, R.string.feedback),
    Rate(R.drawable.ic_rate, R.string.rate),
    About(R.drawable.ic_about, R.string.about)
}

enum class MainMultipleMenu(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    Copy(R.drawable.ic_file_copy, R.string.copy),
    Move(R.drawable.ic_file_moving, R.string.move),
    Delete(R.drawable.ic_delete, R.string.delete),
    Archive(R.drawable.ic_packing, R.string.archive),
}