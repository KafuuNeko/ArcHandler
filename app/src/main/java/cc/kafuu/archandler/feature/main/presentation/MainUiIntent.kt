package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPasteMenuEnum
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

sealed class MainUiIntent {
    data object Init : MainUiIntent()

    data object Back : MainUiIntent()

    data object JumpFilePermissionSetting : MainUiIntent()

    data class MainDrawerMenuClick(
        val menu: MainDrawerMenuEnum
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

    data class MultipleMenuClick(
        val menu: MainMultipleMenuEnum,
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
    ) : MainUiIntent()

    data class PasteMenuClick(
        val menu: MainPasteMenuEnum,
        val targetStorageData: StorageData,
        val targetDirectoryPath: Path,
    ) : MainUiIntent()
}