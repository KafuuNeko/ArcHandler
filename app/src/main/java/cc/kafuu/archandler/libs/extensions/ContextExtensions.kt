package cc.kafuu.archandler.libs.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.model.DefaultAppType
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

/**
 * 根据用户设置打开文件
 * 如果用户设置了默认应用，直接使用该应用打开；否则使用系统选择器
 *
 * @param file 要打开的文件
 * @param title 选择器标题（仅当使用系统选择器时生效）
 */
fun Context.openFileWithDefaultApp(file: File, title: String = "Open with"): Intent? {
    if (!file.exists()) return null
    val mimeType = MimeTypeMap
        .getSingleton()
        .getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"

    // 根据 MIME 类型确定文件类型
    val defaultAppPackage = when {
        mimeType.startsWith("image/") -> AppModel.defaultAppImages
        mimeType.startsWith("video/") -> AppModel.defaultAppVideos
        DefaultAppType.DOCUMENTS.mimeTypes.contains(mimeType.lowercase()) -> AppModel.defaultAppDocuments
        else -> null
    }

    val context = this@openFileWithDefaultApp
    val authority = "${packageName}.fileprovider"
    val fileUri = FileProvider.getUriForFile(context, authority, file)

    return if (defaultAppPackage != null) {
        // 用户设置了默认应用，直接启动该应用
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage(defaultAppPackage)
        }
    } else {
        // 用户没有设置默认应用，使用系统选择器
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.let { Intent.createChooser(it, title) }
    }
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