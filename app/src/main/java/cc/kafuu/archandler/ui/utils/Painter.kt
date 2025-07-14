package cc.kafuu.archandler.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberVideoThumbnailPainter(
    model: Any,
    placeholder: Painter,
    frameMicros: Long = 1_000_000L,
    context: Context = LocalContext.current
): Painter {
    var painter by remember(model) { mutableStateOf<Painter?>(null) }
    LaunchedEffect(model) {
        withContext(Dispatchers.IO) {
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(model)
                    .frame(frameMicros)
                    .submit()
                    .get()

                painter = BitmapPainter(bitmap.asImageBitmap())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return painter ?: placeholder
}
