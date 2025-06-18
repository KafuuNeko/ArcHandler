package cc.kafuu.archandler.libs.archive.model

data class ArchiveEntry(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val compressedSize: Long
)