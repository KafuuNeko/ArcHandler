package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPackMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPasteMenuEnum
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

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
        val enable: Boolean,
        val file: File
    ) : MainUiIntent()

    data class MultipleMenuClick(
        val menu: MainMultipleMenuEnum,
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
    ) : MainUiIntent()

    data class PasteMenuClick(
        val menu: MainPasteMenuEnum,
        val targetStorageData: StorageData = StorageData(),
        val targetDirectoryPath: Path = Path(""),
    ) : MainUiIntent()

    data class PackMenuClick(
        val menu: MainPackMenuEnum,
        val targetStorageData: StorageData = StorageData(),
        val targetDirectoryPath: Path = Path(""),
    ) : MainUiIntent()

    data object SelectAllClick : MainUiIntent()

    data object DeselectClick : MainUiIntent()

    data object SelectAllNoDuplicatesClick : MainUiIntent()

    data object CancelSelectNoDuplicatesJob : MainUiIntent()

    data object CancelUnpackingJob : MainUiIntent()

    data object InvertSelectionClick : MainUiIntent()
}