package cc.kafuu.archandler.libs.core

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun <I, S> Activity.ViewEventCollector(
    viewModel: CoreViewModelWithEvent<I, S>,
    onOtherEvent: suspend (IViewEvent) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.collectEvent(lifecycleOwner) {
            if (it is AppViewEvent) handleAppViewEvent(it) else onOtherEvent(it)
        }
    }
}

sealed class AppViewEvent : IViewEvent {
    data class PopupToastMessage(val message: String) : AppViewEvent()
    data class PopupToastMessageByResId(@StringRes val message: Int) : AppViewEvent()
    data class StartActivity(val activity: Class<*>, val extras: Bundle? = null) : AppViewEvent()
    data class StartActivityByIntent(val intent: Intent) : AppViewEvent()
}

private fun Activity.handleAppViewEvent(viewEvent: AppViewEvent) = when (viewEvent) {
    is AppViewEvent.PopupToastMessage -> {
        Toast.makeText(this, viewEvent.message, Toast.LENGTH_SHORT).show()
    }

    is AppViewEvent.PopupToastMessageByResId -> {
        Toast.makeText(this, getString(viewEvent.message), Toast.LENGTH_SHORT).show()
    }

    is AppViewEvent.StartActivity -> {
        val intent = Intent(this, viewEvent.activity).apply {
            viewEvent.extras?.run { putExtras(this) }
        }
        startActivity(intent)
    }

    is AppViewEvent.StartActivityByIntent -> {
        startActivity(viewEvent.intent)
    }
}