package cc.kafuu.archandler.libs.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cc.kafuu.archandler.ui.theme.AppTheme
import kotlinx.coroutines.launch

abstract class CoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isEnableEdgeToEdge()) {
            enableEdgeToEdge()
        }
        setContent { AppTheme { ViewContent() } }
    }

    protected open fun isEnableEdgeToEdge(): Boolean = true

    @Composable
    protected abstract fun ViewContent()
}

inline fun <I, S, E> CoreActivity.attachEventListener(
    viewModel: CoreViewModel<I, S, E>,
    crossinline onSingleEvent: (event: E) -> Unit
) = lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.singleEventFlow.collect { it?.run { onSingleEvent(this) } }
    }
}