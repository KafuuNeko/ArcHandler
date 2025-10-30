package cc.kafuu.archandler.libs.model

import java.io.File
import java.nio.file.Path

data class CreateArchiveData(
    val files: List<File>,
    val targetStorageData: StorageData,
    val targetDirectoryPath: Path
)