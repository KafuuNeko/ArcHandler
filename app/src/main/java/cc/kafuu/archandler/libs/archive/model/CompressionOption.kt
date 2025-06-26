package cc.kafuu.archandler.libs.archive.model

import java.io.File

sealed class CompressionOption {
    data class Zip(
        val files: List<File>,
        val password: String? = null,
        val compressionLevel: Int = 5
    ) : CompressionOption()

    data class SevenZip(
        val files: List<File>,
        val password: String? = null,
        val compressionLevel: Int = 5
    ) : CompressionOption()

    data class Tar(
        val files: List<File>,
    ) : CompressionOption()

    data class GZip(
        val file: File,
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class BZip2(
        val file: File,
        val compressionLevel: Int = 9
    ) : CompressionOption()
}