package cc.kafuu.archandler.feature.main.presentation

sealed class MainSingleEvent {
    data object JumpFilePermissionSetting : MainSingleEvent()
    data object JumpAboutPage : MainSingleEvent()
    data class PopupToastMessage(val message: String) : MainSingleEvent()
}