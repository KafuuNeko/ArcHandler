package cc.kafuu.archandler.feature.storagepicker.presention

import cc.kafuu.archandler.libs.model.StorageData
import java.io.File

sealed class StoragePickerUiIntent {
    data class Init(val paramsToken: String?) : StoragePickerUiIntent()

    data object Back : StoragePickerUiIntent()

    data object ClosePage : StoragePickerUiIntent()

    data class StorageVolumeSelected(val storageData: StorageData) : StoragePickerUiIntent()

    data object ToStoragePage : StoragePickerUiIntent()

    data class FileSelected(
        val storageData: StorageData,
        val file: File
    ) : StoragePickerUiIntent()

    data object SelectionCompleted : StoragePickerUiIntent()

}