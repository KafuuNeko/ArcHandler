package cc.kafuu.archandler.feature.createarchive.model

import cc.kafuu.archandler.R

enum class ArchiveFormat(
    val displayName: Int,
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
    Xar(
        displayName = R.string.archive_format_xar_name,
        supportsPassword = false,
        supportCompressionTypes = listOf(
            CompressionType.Gzip, CompressionType.Lzma, CompressionType.Bzip2
        ),
        defaultCompressionType = CompressionType.Gzip
    ),
    TarUstar(
        displayName = R.string.archive_format_tar_ustar_name,
        supportsPassword = false,
        supportCompressionTypes = listOf(
            CompressionType.None, CompressionType.Gzip, CompressionType.Xz,
            CompressionType.Bzip2, CompressionType.Lz4, CompressionType.Zstd
        )
    ),
    TarPax(
        displayName = R.string.archive_format_tar_pax_name,
        supportsPassword = false,
        supportCompressionTypes = listOf(
            CompressionType.None, CompressionType.Gzip, CompressionType.Xz,
            CompressionType.Bzip2, CompressionType.Lz4, CompressionType.Zstd
        )
    ),
    TarGnu(
        displayName = R.string.archive_format_tar_gnu_name,
        supportsPassword = false,
        supportCompressionTypes = listOf(
            CompressionType.None, CompressionType.Gzip, CompressionType.Xz,
            CompressionType.Bzip2, CompressionType.Lz4, CompressionType.Zstd
        )
    ),
    TarV7(
        displayName = R.string.archive_format_tar_v7_name,
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