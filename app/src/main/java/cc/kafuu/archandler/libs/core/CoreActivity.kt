package cc.kafuu.archandler.libs.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import cc.kafuu.archandler.ui.theme.AppTheme

abstract class CoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isEnableEdgeToEdge()) {
            enableEdgeToEdge()
        }
        setContent {
            AppTheme { ViewContent() }
        }
    }

    protected open fun isEnableEdgeToEdge(): Boolean = true

    @Composable
    protected abstract fun ViewContent()

}