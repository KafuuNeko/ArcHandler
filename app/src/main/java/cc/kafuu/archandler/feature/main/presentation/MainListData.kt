package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

sealed class MainListData {
    data object Undecided : MainListData()

    data class StorageVolume(
        val storageVolumes: List<StorageData> = emptyList()
    ) : MainListData()

    data class Directory(
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
    ) : MainListData()
}