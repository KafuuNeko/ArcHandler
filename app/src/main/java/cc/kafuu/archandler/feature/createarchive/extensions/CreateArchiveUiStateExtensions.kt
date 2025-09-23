package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import java.io.File

fun CreateArchiveUiState.Normal.getPackageOptions() = when (archiveOptions) {
    is CreateArchiveOptionState.Tar -> listOf(
        CompressionOption.Tar
    )
    is CreateArchiveOptionState.TarWithGZip -> listOf(
        CompressionOption.Tar,
        CompressionOption.GZip(archiveOptions.level)
    )
    is CreateArchiveOptionState.TarWithBZip2-> listOf(
        CompressionOption.Tar,
        CompressionOption.BZip2(archiveOptions.level)
    )

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
}