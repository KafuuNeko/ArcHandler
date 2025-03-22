package cc.kafuu.archandler.libs.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cc.kafuu.archandler.ui.theme.AppTheme
import kotlinx.coroutines.launch

abstract class CoreActivity : ComponentActivity() {
    protected open fun isEnableEdgeToEdge(): Boolean = true

    @Composable
    protected abstract fun ViewContent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        if (isEnableEdgeToEdge()) {
            enableEdgeToEdge()
        }
        setContent { AppTheme(content = getContent()) }
    }

    private fun getContent(): @Composable () -> Unit = {
        Surface(modifier = Modifier.fillMaxSize()) { ViewContent() }
    }
}

inline fun <I, S, E> CoreActivity.attachEventListener(
    viewModel: CoreViewModelWithEvent<I, S, E>,
    crossinline onSingleEvent: (event: E) -> Unit
) = lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.singleEventFlow.collect { it?.run { onSingleEvent(this) } }
    }
}

@Composable
fun ActivityPreview(darkTheme: Boolean, content: @Composable () -> Unit) {
    AppTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), content = content)
    }
}