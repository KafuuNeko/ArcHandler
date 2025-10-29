package cc.kafuu.archandler.feature.storagepicker.model

import cc.kafuu.archandler.libs.model.StorageData
import java.nio.file.Path

sealed class StoragePickerResult {
    data class ChooseDirectory(
        val storageData: StorageData, val directoryPath: Path
    ) : StoragePickerResult()
}