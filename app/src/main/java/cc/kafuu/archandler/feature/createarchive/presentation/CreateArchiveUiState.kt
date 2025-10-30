package cc.kafuu.archandler.feature.createarchive.presentation

import androidx.annotation.StringRes
import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.feature.createarchive.model.CompressionType
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File

sealed class CreateArchiveUiState {
    data object None : CreateArchiveUiState()

    data class Normal(
        val files: List<File>,
        val targetStorageData: StorageData,
        val targetDirectory: File,
        val targetFileName: String = "",
        val archiveOptions: CreateArchiveOptionState = CreateArchiveOptionState(),
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

data class CreateArchiveOptionState(
    val format: ArchiveFormat = ArchiveFormat.Zip,
    val compressionType: CompressionType = format.defaultCompressionType,
    val password: String? = null,
    val level: Int = 5,
)