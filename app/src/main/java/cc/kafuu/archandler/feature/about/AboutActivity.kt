package cc.kafuu.archandler.feature.about

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import cc.kafuu.archandler.feature.about.ui.AboutViewBody
import cc.kafuu.archandler.libs.core.CoreActivity

class AboutActivity : CoreActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    @Composable
    override fun ViewContent() {
        AboutViewBody { finish() }
    }
}