package cc.kafuu.archandler.feature.archiveview.presentation

import android.os.Bundle
import cc.kafuu.archandler.libs.core.IViewEvent

sealed class ArchiveViewViewEvent : IViewEvent {
    data class SelectExtractDirectory(val params: Bundle) : ArchiveViewViewEvent()
}

