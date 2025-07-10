package cc.kafuu.archandler.feature.main.presentation

import java.io.File

sealed class MainViewEvent {
    data object JumpFilePermissionSetting : MainViewEvent()
    data object JumpAboutPage : MainViewEvent()
    data class PopupToastMessage(val message: String) : MainViewEvent()
    data class OpenFile(val file: File, var result: Boolean = false) : MainViewEvent()
}