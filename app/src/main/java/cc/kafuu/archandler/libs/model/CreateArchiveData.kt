package cc.kafuu.archandler.libs.model

import java.io.File

data class CreateArchiveData(
    val files: List<File>,
    val targetDirectory: File
)