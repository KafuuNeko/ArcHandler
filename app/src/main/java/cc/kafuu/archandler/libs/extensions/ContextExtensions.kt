package cc.kafuu.archandler.libs.extensions

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

/**
 * 尝试打开文件
 *
 * @param title 分享对话框的标题
 * @param file 要分享的文件
 */
fun Context.tryOpenFile(title: String, file: File) {
    if (!file.exists()) return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            FileProvider.getUriForFile(this@tryOpenFile, "${packageName}.fileprovider", file),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}