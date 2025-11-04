package cc.kafuu.archandler.libs.archive.model

sealed class CompressionOption {
    abstract val fileExtension: String

    data class Zip(
        val password: String? = null,
        val compressionLevel: Int = 5
    ) : CompressionOption() {
        override val fileExtension: String get() = "zip"
    }

    data class SevenZip(
        val password: String? = null,
        val compressionLevel: Int = 5
    ) : CompressionOption() {
        override val fileExtension: String get() = "7z"
    }

    data class Xar(
        val algorithm: CompressionAlgorithm? = null
    ) : CompressionOption() {
        override val fileExtension: String
            get() = "xar"
    }

    data class Raw(
        val algorithm: CompressionAlgorithm
    ) : CompressionOption() {
        override val fileExtension: String
            get() = algorithm.getNameExtension()
    }

    data class Tar(
        val algorithm: CompressionAlgorithm? = null
    ) : CompressionOption() {
        override val fileExtension: String
            get() = "tar${(algorithm?.let { ".${it.getNameExtension()}" } ?: "")}"
    }

    data class Cpio(
        val algorithm: CompressionAlgorithm? = null
    ) : CompressionOption() {
        override val fileExtension: String
            get() = "cpio${(algorithm?.let { ".${it.getNameExtension()}" } ?: "")}"
    }
}

private fun CompressionAlgorithm.getNameExtension() = when (this) {
    is CompressionAlgorithm.BZip2 -> "bz2"
    is CompressionAlgorithm.GZip -> "gz"
    is CompressionAlgorithm.Lz4 -> "lz4"
    is CompressionAlgorithm.Xz -> "xz"
    is CompressionAlgorithm.Zstd -> "zst"
}