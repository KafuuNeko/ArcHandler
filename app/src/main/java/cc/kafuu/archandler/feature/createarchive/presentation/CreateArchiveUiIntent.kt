package cc.kafuu.archandler.feature.createarchive.presentation

import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.feature.createarchive.model.CompressionType
import cc.kafuu.archandler.libs.archive.model.SplitUnit

sealed class CreateArchiveUiIntent {
    data class Init(val transferId: String?) : CreateArchiveUiIntent()

    data object Back : CreateArchiveUiIntent()

    data class ArchiveFormatChange(val format: ArchiveFormat) : CreateArchiveUiIntent()

    data class ArchiveCompressionTypeChange(val type: CompressionType) : CreateArchiveUiIntent()

    data class TargetFileNameChange(val name: String) : CreateArchiveUiIntent()

    data class CompressLevelChange(val level: Int) : CreateArchiveUiIntent()

    data class ArchivePasswordChange(val password: String) : CreateArchiveUiIntent()

    data object CreateArchive : CreateArchiveUiIntent()

    data object CancelPackingJob : CreateArchiveUiIntent()

    data object SelectFolder : CreateArchiveUiIntent()

    data class SelectFolderCompleted(val data: String) : CreateArchiveUiIntent()
}