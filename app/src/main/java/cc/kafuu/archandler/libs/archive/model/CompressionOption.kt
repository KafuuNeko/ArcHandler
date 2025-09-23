package cc.kafuu.archandler.libs.archive.model

sealed class CompressionOption {
    data class Zip(
        val password: String? = null,
        val compressionLevel: Int = 5
    ) : CompressionOption()

    data class SevenZip(
        val password: String? = null,
        val compressionLevel: Int = 5
    ) : CompressionOption()

    data object Tar : CompressionOption()

    data class GZip(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class BZip2(
        val compressionLevel: Int = 9
    ) : CompressionOption()
}