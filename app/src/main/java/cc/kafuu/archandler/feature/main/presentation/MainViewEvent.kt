package cc.kafuu.archandler.feature.main.presentation

import android.os.Bundle
import cc.kafuu.archandler.libs.core.IViewEvent

sealed class MainViewEvent : IViewEvent {
    data object JumpFilePermissionSetting : MainViewEvent()
    
    data class StartArchiveViewActivity(val params: Bundle) : MainViewEvent()
}