package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.feature.createarchive.model.CompressionType
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import cc.kafuu.archandler.libs.archive.model.CompressionOption.Cpio
import cc.kafuu.archandler.libs.archive.model.CompressionOption.CpioBzip2
import cc.kafuu.archandler.libs.archive.model.CompressionOption.CpioGZip
import cc.kafuu.archandler.libs.archive.model.CompressionOption.CpioXz
import cc.kafuu.archandler.libs.archive.model.CompressionOption.SevenZip
import cc.kafuu.archandler.libs.archive.model.CompressionOption.Tar
import cc.kafuu.archandler.libs.archive.model.CompressionOption.TarBzip2
import cc.kafuu.archandler.libs.archive.model.CompressionOption.TarGZip
import cc.kafuu.archandler.libs.archive.model.CompressionOption.TarXz
import cc.kafuu.archandler.libs.archive.model.CompressionOption.Zip

fun CreateArchiveUiState.Normal.getPackageOptions() = when (archiveOptions.format) {
    ArchiveFormat.Zip -> listOf(
        Zip(
            password = archiveOptions.password?.takeIf { it.isNotEmpty() },
            compressionLevel = if (archiveOptions.compressionType.levelRange == null) 0 else archiveOptions.level
        )
    )

    ArchiveFormat.SevenZip -> listOf(
        SevenZip(
            password = archiveOptions.password?.takeIf { it.isNotEmpty() },
            compressionLevel = if (archiveOptions.compressionType.levelRange == null) 0 else archiveOptions.level
        )
    )

    ArchiveFormat.Tar -> listOf(
        when (archiveOptions.compressionType) {
            CompressionType.Gzip -> TarGZip(compressionLevel = archiveOptions.level)
            CompressionType.Bzip2 -> TarBzip2(compressionLevel = archiveOptions.level)
            CompressionType.Xz -> TarXz(compressionLevel = archiveOptions.level)
            CompressionType.Lz4 -> CompressionOption.TarLz4
            else -> Tar
        }
    )

    ArchiveFormat.Cpio -> listOf(
        when (archiveOptions.compressionType) {
            CompressionType.Gzip -> CpioGZip(compressionLevel = archiveOptions.level)
            CompressionType.Bzip2 -> CpioBzip2(compressionLevel = archiveOptions.level)
            CompressionType.Xz -> CpioXz(compressionLevel = archiveOptions.level)
            CompressionType.Lz4 -> CompressionOption.CpioLz4
            else -> Cpio
        }
    )
}