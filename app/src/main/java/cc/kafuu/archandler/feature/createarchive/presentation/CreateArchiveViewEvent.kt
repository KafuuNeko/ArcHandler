package cc.kafuu.archandler.feature.createarchive.presentation

import androidx.annotation.StringRes

sealed class CreateArchiveViewEvent {
    data class ToastMessageByResId(@StringRes val message: Int) : CreateArchiveViewEvent()
}