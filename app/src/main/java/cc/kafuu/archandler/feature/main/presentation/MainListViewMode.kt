package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

sealed class MainListViewMode {
    data object Normal : MainListViewMode()

    data class MultipleSelect(
        val selected: Set<File> = emptySet()
    ) : MainListViewMode()

    data class Pause(
        val sourceStorageData: StorageData,
        val sourceDirectoryPath: Path,
        val sourceFiles: List<File>,
        val isMoving: Boolean = false
    ) : MainListViewMode()
}
