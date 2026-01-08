package cc.kafuu.archandler.feature.archiveview.presentation

import androidx.compose.foundation.lazy.LazyListState
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.model.LayoutType
import cc.kafuu.archandler.libs.utils.DeferredResult
import java.io.File

sealed class ArchiveViewUiState {
    data object None : ArchiveViewUiState()

    data class Normal(
        val archiveFile: File,
        val currentPath: String = "",
        val entries: List<ArchiveEntry> = emptyList(),
        val loadState: ArchiveViewLoadState = ArchiveViewLoadState.None,
        val lazyListState: LazyListState = LazyListState(),
        val layoutType: LayoutType = LayoutType.LIST
    ) : ArchiveViewUiState()

    data object Finished : ArchiveViewUiState()
}

sealed class ArchiveViewLoadState {
    data object None : ArchiveViewLoadState()

    data object ArchiveOpening : ArchiveViewLoadState()

    data object LoadingEntries : ArchiveViewLoadState()

    data class Extracting(
        val index: Int,
        val path: String,
        val total: Int
    ) : ArchiveViewLoadState()
}

