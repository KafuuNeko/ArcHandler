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

    data class GZip(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class BZip2(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data object Tar : CompressionOption()

    data class TarGZip(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class TarBzip2(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class TarXz(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data object TarLz4 : CompressionOption()

    data object Cpio : CompressionOption()

    data class CpioGZip(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class CpioBzip2(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data class CpioXz(
        val compressionLevel: Int = 6
    ) : CompressionOption()

    data object CpioLz4 : CompressionOption()
}