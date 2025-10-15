package cc.kafuu.archandler.feature.about

import androidx.compose.runtime.Composable
import cc.kafuu.archandler.feature.about.ui.AboutViewBody
import cc.kafuu.archandler.libs.core.CoreActivity

class AboutActivity : CoreActivity() {
    @Composable
    override fun ViewContent() {
        AboutViewBody { finish() }
    }
}