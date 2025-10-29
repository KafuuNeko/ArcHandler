package cc.kafuu.archandler.feature.storagepicker.presention

import cc.kafuu.archandler.feature.storagepicker.model.PickMode
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

sealed class StoragePickerUiState {
    data object None : StoragePickerUiState()
    data class Normal(
        val pickMode: PickMode = PickMode.ChooseDirectory,
        val loadState: StoragePickerLoadState = StoragePickerLoadState.None,
        val listState: StoragePickerListState = StoragePickerListState.Undecided
    ) : StoragePickerUiState()

    data object Finished : StoragePickerUiState()
}

sealed class StoragePickerLoadState {
    data object None : StoragePickerLoadState()

    data object ExternalStoragesLoading : StoragePickerLoadState()

    data object DirectoryLoading : StoragePickerLoadState()
}

sealed class StoragePickerListState {
    data object Undecided : StoragePickerListState()

    data class StorageVolume(
        val storageVolumes: List<StorageData> = emptyList()
    ) : StoragePickerListState()

    data class Directory(
        val storageData: StorageData,
        val directoryPath: Path,
        val files: List<File> = emptyList(),
        val canRead: Boolean = true,
        val canWrite: Boolean = true
    ) : StoragePickerListState()
}