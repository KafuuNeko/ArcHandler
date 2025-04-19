package cc.kafuu.archandler.feature.main.presentation

sealed class MainViewEvent {
    data object JumpFilePermissionSetting : MainViewEvent()
    data object JumpAboutPage : MainViewEvent()
    data class PopupToastMessage(val message: String) : MainViewEvent()
}