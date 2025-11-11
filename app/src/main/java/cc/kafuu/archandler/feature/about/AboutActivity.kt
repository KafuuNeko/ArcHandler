package cc.kafuu.archandler.feature.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.about.ui.AboutLayout
import cc.kafuu.archandler.libs.core.CoreActivity

class AboutActivity : CoreActivity() {
    @Composable
    override fun ViewContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            AboutLayout { finish() }
        }
    }
}