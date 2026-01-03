package cc.kafuu.archandler.feature.archiveview.presentation

import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import java.io.File

sealed class ArchiveViewUiIntent {
    data class Init(val transferId: String?) : ArchiveViewUiIntent()

    data object Back : ArchiveViewUiIntent()

    data class EntrySelected(val entry: ArchiveEntry) : ArchiveViewUiIntent()

    data object ExtractArchive : ArchiveViewUiIntent()

    data class ExtractToDirectoryCompleted(val transferId: String?) : ArchiveViewUiIntent()

    data object CancelExtracting : ArchiveViewUiIntent()
}

