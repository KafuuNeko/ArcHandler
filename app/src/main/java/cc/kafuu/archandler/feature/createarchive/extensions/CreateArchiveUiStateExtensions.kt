package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.libs.archive.model.CompressionOption

fun CreateArchiveUiState.Normal.getPackageOptions() = when (archiveOptions) {
    is CreateArchiveOptionState.SevenZip -> listOf(
        CompressionOption.SevenZip(
            password = archiveOptions.password?.takeIf { it.isNotEmpty() },
            compressionLevel = archiveOptions.level
        )
    )

    is CreateArchiveOptionState.Zip -> listOf(
        CompressionOption.Zip(
            password = archiveOptions.password?.takeIf { it.isNotEmpty() },
            compressionLevel = archiveOptions.level
        )
    )

    is CreateArchiveOptionState.Tar -> listOf(
        CompressionOption.Tar
    )

    is CreateArchiveOptionState.TarWithGZip -> listOf(
        CompressionOption.Tar,
        CompressionOption.GZip(archiveOptions.level)
    )

    is CreateArchiveOptionState.TarWithBZip2 -> listOf(
        CompressionOption.Tar,
        CompressionOption.BZip2(archiveOptions.level)
    )

    is CreateArchiveOptionState.TarWithXz -> listOf(
        CompressionOption.TarXz(compressionLevel = archiveOptions.level)
    )

    is CreateArchiveOptionState.Cpio -> listOf(
        CompressionOption.Cpio
    )

    is CreateArchiveOptionState.CpioWithBZip2 -> listOf(
        CompressionOption.CpioBzip2(compressionLevel = archiveOptions.level)
    )

    is CreateArchiveOptionState.CpioWithGZip -> listOf(
        CompressionOption.CpioGZip(compressionLevel = archiveOptions.level)
    )

    is CreateArchiveOptionState.CpioWithXz -> listOf(
        CompressionOption.CpioXz(compressionLevel = archiveOptions.level)
    )

}