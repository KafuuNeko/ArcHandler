package cc.kafuu.archandler.libs.extensions

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

/**
 * 创建打开文件方式选择器意图
 *
 * @param title 分享对话框的标题
 * @param file 要分享的文件
 */
fun Context.createChooserIntent(title: String, file: File): Intent? {
    if (!file.exists()) return null
    val context = this@createChooserIntent
    val authority = "${packageName}.fileprovider"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            FileProvider.getUriForFile(context, authority, file),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(intent, title)
}