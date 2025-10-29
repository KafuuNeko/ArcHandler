package cc.kafuu.archandler.feature.storagepicker.model

import cc.kafuu.archandler.libs.model.StorageData

data class StoragePickerParams(
    val pickMode: PickMode,
    val defaultStorage: StorageData? = null,
    val defaultPath: String? = null,
)