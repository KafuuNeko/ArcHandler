package cc.kafuu.archandler.libs.archive.model

sealed class CompressionAlgorithm {
    abstract val compressionLevel: Int

    data class GZip(
        override val compressionLevel: Int = 6
    ) : CompressionAlgorithm()

    data class BZip2(
        override val compressionLevel: Int = 6
    ) : CompressionAlgorithm()

    data class Xz(
        override val compressionLevel: Int = 6
    ) : CompressionAlgorithm()

    data class Lz4(
        override val compressionLevel: Int = 6
    ) : CompressionAlgorithm()

    data class Zstd(
        override val compressionLevel: Int = 6
    ) : CompressionAlgorithm()
}