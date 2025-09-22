package cc.kafuu.archandler.feature.createarchive.presentation

import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.libs.archive.model.SplitUnit

sealed class CreateArchiveUiIntent {
    data class OnCreate(val transferId: String?) : CreateArchiveUiIntent()

    data object Back : CreateArchiveUiIntent()

    data class ArchiveFormatChange(val format: ArchiveFormat) : CreateArchiveUiIntent()

    data object PickSavePath : CreateArchiveUiIntent()

    data class TargetFileNameChange(val name: String) : CreateArchiveUiIntent()

    data class CompressLevelChange(val level: Int) : CreateArchiveUiIntent()

    data class ArchivePasswordChange(val password: String) : CreateArchiveUiIntent()

    data class SplitEnabledToggle(val isEnable: Boolean) : CreateArchiveUiIntent()

    data class SplitUnitChange(val unit: SplitUnit) : CreateArchiveUiIntent()

    data class SplitSizeChange(val size: Long?) : CreateArchiveUiIntent()
}