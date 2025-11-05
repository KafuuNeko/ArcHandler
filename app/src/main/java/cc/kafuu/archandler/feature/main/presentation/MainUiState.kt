package cc.kafuu.archandler.feature.main.presentation

import androidx.compose.foundation.lazy.LazyListState
import cc.kafuu.archandler.libs.model.FileConflictStrategy
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.libs.utils.DeferredResult
import cc.kafuu.archandler.ui.utils.Stack
import java.io.File
import java.nio.file.Path

sealed class MainUiState {
    data object None : MainUiState()

    data object PermissionDenied : MainUiState()

    data class Normal(
        val loadState: MainLoadState = MainLoadState.None,
        val dialogState: MainDialogState = MainDialogState.None,
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState()

    data object Finished : MainUiState()
}

sealed class MainDialogState {
    data object None : MainDialogState()

    data class PasswordInput(
        val file: File,
        val deferredResult: DeferredResult<String?> = DeferredResult()
    ) : MainDialogState()

    data class FileDeleteConfirm(
        val fileSet: Set<File>,
        val deferredResult: DeferredResult<Boolean> = DeferredResult()
    ) : MainDialogState()

    data class CreateDirectoryInput(
        val deferredResult: DeferredResult<String?> = DeferredResult()
    ) : MainDialogState()

    data class RenameInput(
        val defaultName: String,
        val deferredResult: DeferredResult<String?> = DeferredResult()
    ) : MainDialogState()

    data class FileConflict(
        val oldFile: File,
        val newFile: File,
        val deferredResult: DeferredResult<Pair<FileConflictStrategy, Boolean>?> = DeferredResult()
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
        val canWrite: Boolean = true,
        val lazyListState: LazyListState = LazyListState()
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
        val restoreStack: Stack<MainListState>,
        val isMoving: Boolean = false
    ) : MainListViewModeState()

    data class Pack(
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
        val restoreStack: Stack<MainListState>
    ) : MainListViewModeState()
}

sealed class MainLoadState {
    data object None : MainLoadState()

    data object ExternalStoragesLoading : MainLoadState()

    data object DirectoryLoading : MainLoadState()

    data object FileScanning : MainLoadState()

    data class Pasting(
        val isMoving: Boolean,
        val src: File,
        val dest: File,
        val totality: Int,
        val currentIndex: Int
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