package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.feature.createarchive.model.CompressionType
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.libs.archive.model.CompressionAlgorithm
import cc.kafuu.archandler.libs.archive.model.CompressionOption.Cpio
import cc.kafuu.archandler.libs.archive.model.CompressionOption.SevenZip
import cc.kafuu.archandler.libs.archive.model.CompressionOption.Tar
import cc.kafuu.archandler.libs.archive.model.CompressionOption.Xar
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
        Tar(algorithm = getPackageAlgorithm())
    )

    ArchiveFormat.Cpio -> listOf(
        Cpio(algorithm = getPackageAlgorithm())
    )

    ArchiveFormat.Xar -> listOf(
        Xar(algorithm = getPackageAlgorithm())
    )
}

fun CreateArchiveUiState.Normal.getPackageAlgorithm() = when (archiveOptions.compressionType) {
    CompressionType.Gzip -> CompressionAlgorithm.GZip(archiveOptions.level)
    CompressionType.Bzip2 -> CompressionAlgorithm.BZip2(archiveOptions.level)
    CompressionType.Xz -> CompressionAlgorithm.Xz(archiveOptions.level)
    CompressionType.Zstd -> CompressionAlgorithm.Zstd(archiveOptions.level)
    CompressionType.Lz4 -> CompressionAlgorithm.Lz4(archiveOptions.level)
    else -> null
}