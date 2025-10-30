package cc.kafuu.archandler.feature.createarchive.model

import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class ArchiveFormat(
    @StringRes val displayName: Int,
    val supportsPassword: Boolean,
    val supportCompressionTypes: List<CompressionType>,
    val defaultCompressionType: CompressionType = CompressionType.None
) {
    Zip(
        displayName = R.string.archive_format_zip_name,
        supportsPassword = true,
        supportCompressionTypes = listOf(CompressionType.Store, CompressionType.Deflate),
        defaultCompressionType = CompressionType.Deflate,
    ),
    SevenZip(
        displayName = R.string.archive_format_seven_zip_name,
        supportsPassword = true,
        supportCompressionTypes = listOf(CompressionType.Store, CompressionType.Lzma),
        defaultCompressionType = CompressionType.Lzma
    ),
    Tar(
        displayName = R.string.archive_format_tar_name,
        supportsPassword = false,
        supportCompressionTypes = listOf(
            CompressionType.None, CompressionType.Gzip, CompressionType.Xz,
            CompressionType.Bzip2, CompressionType.Lz4, CompressionType.Zstd
        )
    ),
    Cpio(
        displayName = R.string.archive_format_cpio_name,
        supportsPassword = false,
        supportCompressionTypes = listOf(
            CompressionType.None, CompressionType.Gzip, CompressionType.Xz,
            CompressionType.Bzip2, CompressionType.Lz4, CompressionType.Zstd
        )
    ),
}