package cc.kafuu.archandler.feature.archiveview.presentation

import cc.kafuu.archandler.libs.archive.model.ArchiveEntry

sealed class ArchiveViewUiIntent {
    data class Init(val transferId: String?) : ArchiveViewUiIntent()

    data object Back : ArchiveViewUiIntent()

    data object Close : ArchiveViewUiIntent()

    data class EntrySelected(val entry: ArchiveEntry) : ArchiveViewUiIntent()

    data class PathSelected(val path: String) : ArchiveViewUiIntent()

    data object ExtractArchive : ArchiveViewUiIntent()

    data class ExtractToDirectoryCompleted(val transferId: String?) : ArchiveViewUiIntent()

    data object CancelExtracting : ArchiveViewUiIntent()

    data class SwitchLayoutType(val layoutType: cc.kafuu.archandler.libs.model.LayoutType) : ArchiveViewUiIntent()
}

