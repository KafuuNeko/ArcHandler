package cc.kafuu.archandler.libs.ext

import cc.kafuu.archandler.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun File.getIcon(): Int {
    if (isDirectory) {
        return R.drawable.ic_folder
    }

    val archiveExtensions = listOf(
        ".zip", ".rar", ".tar", ".gz", ".7z", ".bz2", ".xz", ".tar.gz", ".tar.bz2", ".tar.xz",
        ".lzma", ".lz", ".cab", ".iso", ".apk", ".tgz", ".tar.Z", ".cpio", ".ar", ".jar", ".war"
    )
    if (archiveExtensions.any { name.endsWith(it, true) }) {
        return R.drawable.ic_archive
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