package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.core.IViewEvent

sealed class MainViewEvent : IViewEvent {
    data object JumpFilePermissionSetting : MainViewEvent()
}