package cc.kafuu.archandler.feature.createarchive.presentation

import androidx.annotation.StringRes
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressEncryptable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressLevelConfigurable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressSplittable
import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.libs.archive.model.SplitUnit
import java.io.File

sealed class CreateArchiveUiState {
    data object None : CreateArchiveUiState()

    data class Normal(
        val files: List<File>,
        val targetDirectory: File,
        val targetFileName: String = "",
        val archiveOptions: CreateArchiveOptionState = CreateArchiveOptionState.Zip(),
        val loadState: CreateArchiveLoadState = CreateArchiveLoadState.None,
    ) : CreateArchiveUiState()

    data object Finished : CreateArchiveUiState()
}

sealed class CreateArchiveLoadState {
    data object None : CreateArchiveLoadState()

    data class Packing(
        @StringRes val message: Int,
        val currentFile: File? = null,
        val progression: Pair<Int, Int>? = null
    ) : CreateArchiveLoadState()
}

sealed class CreateArchiveOptionState {
    abstract val format: ArchiveFormat

    data class Zip(
        override val format: ArchiveFormat = ArchiveFormat.Zip,
        override val password: String? = null,
        override val level: Int = 5,
        override val splitEnabled: Boolean = false,
        override val splitSize: Long? = null,
        override val splitUnit: SplitUnit = SplitUnit.MB,
    ) : CreateArchiveOptionState(),
        CompressEncryptable, CompressLevelConfigurable, CompressSplittable

    data class SevenZip(
        override val format: ArchiveFormat = ArchiveFormat.SevenZip,
        override val password: String? = null,
        override val level: Int = 5,
        override val splitEnabled: Boolean = false,
        override val splitSize: Long? = null,
        override val splitUnit: SplitUnit = SplitUnit.MB,
    ) : CreateArchiveOptionState(),
        CompressEncryptable, CompressLevelConfigurable, CompressSplittable

    data class Tar(
        override val format: ArchiveFormat = ArchiveFormat.Tar
    ) : CreateArchiveOptionState()

    data class TarWithGZip(
        override val format: ArchiveFormat = ArchiveFormat.TarWithGZip,
        override val level: Int = 5,
    ) : CreateArchiveOptionState(), CompressLevelConfigurable

    data class TarWithBZip2(
        override val format: ArchiveFormat = ArchiveFormat.TarWithBZip2,
        override val level: Int = 5,
    ) : CreateArchiveOptionState(), CompressLevelConfigurable
}
