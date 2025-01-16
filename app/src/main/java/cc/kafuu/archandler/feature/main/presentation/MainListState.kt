package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

sealed class MainListState {
    data object Undecided : MainListState()

    data class StorageVolume(
        val storageVolumes: List<StorageData> = emptyList()
    ) : MainListState()

    data class Directory(
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
    ) : MainListState()
}