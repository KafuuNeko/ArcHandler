package cc.kafuu.archandler.feature.createarchive.presentation

import android.os.Bundle
import cc.kafuu.archandler.feature.storagepicker.model.StoragePickerParams
import cc.kafuu.archandler.libs.core.IViewEvent
import cc.kafuu.archandler.libs.utils.DeferredResult

sealed class CreateArchiveViewEvent: IViewEvent {
    data class SelectFolder(
        val params: Bundle,
        val result: DeferredResult<String?> = DeferredResult()
    ) : CreateArchiveViewEvent()
}