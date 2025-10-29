package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.libs.utils.DeferredResult
import java.io.File
import java.nio.file.Path

sealed class MainUiState {
    data object None : MainUiState()

    data object PermissionDenied : MainUiState()

    data class Normal(
        val loadState: MainLoadState = MainLoadState.None,
        val dialogStates: Set<MainDialogState> = emptySet(),
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState()

    data object Finished : MainUiState()
}

sealed class MainDialogState {
    data class PasswordInput(
        val file: File,
        val deferredResult: DeferredResult<String?> = DeferredResult()
    ) : MainDialogState()

    data class FileDeleteConfirm(
        val fileSet: Set<File>,
        val deferredResult: DeferredResult<Boolean> = DeferredResult()
    ) : MainDialogState()
}

sealed class MainListState {
    data object Undecided : MainListState()

    data class StorageVolume(
        val storageVolumes: List<StorageData> = emptyList()
    ) : MainListState()

    data class Directory(
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
        val canRead: Boolean = true,
        val canWrite: Boolean = true
    ) : MainListState()
}

sealed class MainListViewModeState {
    data object Normal : MainListViewModeState()

    data class MultipleSelect(
        val selected: Set<File> = emptySet()
    ) : MainListViewModeState()

    data class Paste(
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
        val isMoving: Boolean = false
    ) : MainListViewModeState()

    data class Pack(
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
    ) : MainListViewModeState()
}

sealed class MainLoadState {
    data object None : MainLoadState()

    data object ExternalStoragesLoading : MainLoadState()

    data object DirectoryLoading : MainLoadState()

    data class Pasting(
        val isMoving: Boolean,
        val src: File,
        val dest: File,
        val totality: Int,
        val quantityCompleted: Int
    ) : MainLoadState()

    data class ArchiveOpening(
        val file: File
    ) : MainLoadState()

    data class Unpacking(
        val file: File,
        val index: Int,
        val path: String,
        val target: Int
    ) : MainLoadState()

    data class FilesDeleting(
        val file: File
    ) : MainLoadState()

    data class QueryDuplicateFiles(
        val file: File? = null
    ) : MainLoadState()
}