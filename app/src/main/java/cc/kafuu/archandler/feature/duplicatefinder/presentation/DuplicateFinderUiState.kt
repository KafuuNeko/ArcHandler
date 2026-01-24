package cc.kafuu.archandler.feature.duplicatefinder.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import cc.kafuu.archandler.libs.utils.DeferredResult
import java.io.File

sealed class DuplicateFinderUiState {
    data object None : DuplicateFinderUiState()

    data class Normal(
        val loadState: DuplicateFinderLoadState = DuplicateFinderLoadState.None,
        val dialogState: DuplicateFinderDialogState = DuplicateFinderDialogState.None,
        val searchState: DuplicateFinderSearchState = DuplicateFinderSearchState.Idle,
        val selectedFiles: Set<File> = emptySet()
    ) : DuplicateFinderUiState()

    data object Finished : DuplicateFinderUiState()
}

sealed class DuplicateFinderDialogState {
    data object None : DuplicateFinderDialogState()

    data class DeleteConfirm(
        val fileSet: Set<File>,
        val deferredResult: DeferredResult<Boolean> = DeferredResult()
    ) : DuplicateFinderDialogState()
}

sealed class DuplicateFinderSearchState {
    data object Idle : DuplicateFinderSearchState()

    data object Searching : DuplicateFinderSearchState()

    data class Success(
        val duplicateGroups: List<DuplicateFileGroup>,
        val totalFiles: Int,
        val duplicateFileCount: Int,
        val wastedSpace: Long
    ) : DuplicateFinderSearchState()

    data class Error(
        val message: String
    ) : DuplicateFinderSearchState()
}

sealed class DuplicateFinderLoadState {
    data object None : DuplicateFinderLoadState()

    data class Scanning(
        val currentFile: File? = null,
        val scannedCount: Int = 0,
        val totalCount: Int = 0
    ) : DuplicateFinderLoadState()

    data class Hashing(
        val currentFile: File,
        val processedCount: Int,
        val totalCount: Int
    ) : DuplicateFinderLoadState()

    data class Deleting(
        val deletedCount: Int,
        val totalCount: Int
    ) : DuplicateFinderLoadState()
}

data class DuplicateFileGroup(
    val hash: String,
    val fileSize: Long,
    val files: List<File>
)
