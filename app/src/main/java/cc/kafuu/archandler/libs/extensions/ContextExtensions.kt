package cc.kafuu.archandler.libs.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

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


fun Context.getFileNameWithExtension(uri: Uri, emptySuffix: String = "unknow_"): String {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor =
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (!it.moveToFirst()) return@use
            name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        }
    }
    if (name.isNullOrEmpty()) {
        name = uri.lastPathSegment ?: "${emptySuffix}${System.currentTimeMillis()}"
        if (!name.contains('.') && contentResolver.getType(uri) != null) {
            val extension =
                MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
            if (!extension.isNullOrEmpty()) name += ".$extension"
        }
    }
    return name
}

fun Context.saveFile(uri: Uri, outFile: File) {
    try {
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.canOpenUri(uri: Uri) = try {
    contentResolver.openInputStream(uri)?.close()
    true
} catch (e: Exception) {
    false
}