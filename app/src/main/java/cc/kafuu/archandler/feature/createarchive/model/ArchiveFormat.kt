package cc.kafuu.archandler.feature.createarchive.model

import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class ArchiveFormat(
    @StringRes val displayName: Int,
    val supportsPassword: Boolean,
    val supportsLevel: Boolean,
    val supportsSplit: Boolean,
    val levelRange: IntRange?,
) {
    Zip(
        displayName = R.string.archive_format_zip_name,
        supportsPassword = true,
        supportsLevel = true,
        supportsSplit = true,
        levelRange = 0..9
    ),
    SevenZip(
        displayName = R.string.archive_format_seven_zip_name,
        supportsPassword = true,
        supportsLevel = true,
        supportsSplit = true,
        levelRange = 0..9
    ),
    Tar(
        displayName = R.string.archive_format_tar_name,
        supportsPassword = false,
        supportsLevel = false,
        supportsSplit = false,
        levelRange = null
    ),
    TarWithGZip(
        displayName = R.string.archive_format_tar_with_gzip_name,
        supportsPassword = false,
        supportsLevel = true,
        supportsSplit = false,
        levelRange = 1..9
    ),
    TarWithBZip2(
        displayName = R.string.archive_format_tar_with_bzip_ii_name,
        supportsPassword = false,
        supportsLevel = true,
        supportsSplit = false,
        levelRange = 1..9,
    ),
}