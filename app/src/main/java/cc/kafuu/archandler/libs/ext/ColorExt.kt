package cc.kafuu.archandler.libs.ext

import androidx.compose.ui.graphics.Color

fun Color.withAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha)
}