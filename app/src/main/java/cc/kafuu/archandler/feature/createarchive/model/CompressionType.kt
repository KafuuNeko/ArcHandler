package cc.kafuu.archandler.feature.createarchive.model

import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class CompressionType(
    @StringRes val displayName: Int,
    val levelRange: IntRange?,
) {
    None(
        displayName = R.string.archive_compression_type_none_name,
        levelRange = null
    ),
    Store(
        displayName = R.string.archive_compression_type_store_name,
        levelRange = null
    ),
    Deflate(
        displayName = R.string.archive_compression_type_deflate_name,
        levelRange = 1..9
    ),
    Lzma(
        displayName = R.string.archive_compression_type_lzma_name,
        levelRange = 1..9
    ),
    Gzip(
        displayName = R.string.archive_compression_type_gzip_name,
        levelRange = 1..9
    ),
    Bzip2(
        displayName = R.string.archive_compression_type_bzip2_name,
        levelRange = 1..9
    ),
    Xz(
        displayName = R.string.archive_compression_type_xz_name,
        levelRange = 1..9
    ),
    Lz4(
        displayName = R.string.archive_compression_type_lz4_name,
        levelRange = null
    ),
    Zstd(
        displayName = R.string.archive_compression_type_zstd_name,
        levelRange = 1 .. 19
    )
}