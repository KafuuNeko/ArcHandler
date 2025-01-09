package cc.kafuu.archandler.libs.ext

import cc.kafuu.archandler.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

private val EXTENSION_ICON_MAP = mapOf(
    // archive
    R.drawable.ic_archive to setOf(
        ".zip", ".rar", ".tar", ".gz", ".7z", ".bz2", ".xz", ".tar.gz", ".tar.bz2", ".tar.xz",
        ".lzma", ".lz", ".cab", ".iso", ".apk", ".tgz", ".tar.Z", ".cpio", ".ar", ".jar", ".war"
    ),

    // image
    R.drawable.ic_file_image to setOf(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".raw", ".svg", ".heif"
    ),

    // pdf
    R.drawable.ic_file_pdf to setOf(".pdf"),

    // docs
    R.drawable.ic_file_docs to setOf(
        ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".odp", ".txt", ".rtf",
        ".md"
    ),

    // movie
    R.drawable.ic_file_movie to setOf(
        ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".mpg", ".mpeg", ".3gp", ".ogg"
    ),

    // music
    R.drawable.ic_file_music to setOf(
        ".mp3", ".wav", ".aac", ".flac", ".ogg", ".m4a", ".wma", ".aiff"
    ),

    // database
    R.drawable.ic_file_database to setOf(
        ".db", ".sqlite", ".sqlite3", ".db3", ".mdb", ".accdb"
    )
)

fun File.getIcon(): Int {
    if (isDirectory) return R.drawable.ic_folder
    val extension = name.lowercase()
    for ((icon, extensions) in EXTENSION_ICON_MAP) {
        if (extensions.any { extension.endsWith(it, true) }) {
            return icon
        }
    }
    return R.drawable.ic_file
}


fun File.getLastModifiedDate(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(Date(lastModified()))
}

fun File.getReadableSize(): String {
    val size = length()
    if (size == 0L) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    val unitIndex = (log10(size.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)
    val readableSize = size / 1024.0.pow(unitIndex.toDouble())

    return "%.2f %s".format(readableSize, units[unitIndex])
}