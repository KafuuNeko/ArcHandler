package cc.kafuu.archandler.libs.ext

import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.archive.ArchiveManager
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

private val EXTENSION_ICON_MAP = mapOf(
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
    if (ArchiveManager.isExtractable(this)) return R.drawable.ic_archive
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

fun File.appMoveTo(dest: File): Boolean {
    try {
        if (isDirectory) {
            FileUtils.moveDirectory(this, dest)
        } else {
            FileUtils.moveFile(this, dest)
        }
        return true
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
}

fun File.appCopyTo(dest: File): Boolean {
    try {
        if (isDirectory) {
            FileUtils.copyDirectory(this, dest)
        } else {
            FileUtils.copyFile(this, dest)
        }
        return true
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
}

fun File.createUniqueDirectory(): File? {
    var dir = this
    var index = 1
    while (dir.exists()) {
        val baseName = this.nameWithoutExtension
        val parent = this.parentFile
        val newName = "$baseName($index)"
        dir = File(parent, newName)
        index++
    }
    if (!dir.mkdirs()) return null
    return dir
}

fun File.getSameNameDirectory(): File {
    val name = if (this.isFile) {
        this.nameWithoutExtension
    } else {
        this.name
    }
    return File(this.parentFile, name)
}

fun List<File>.deletes(
    onStartDelete: ((file: File) -> Unit)? = null,
    onFinishedDelete: ((file: File, isSuccessful: Boolean) -> Unit)? = null
) {
    for (file in this) {
        if (file.isDirectory) {
            file.listFiles()?.asList()?.deletes(onStartDelete, onFinishedDelete)
            file.delete()
            continue
        }
        onStartDelete?.invoke(file)
        val isSuccessful = file.delete()
        onFinishedDelete?.invoke(file, isSuccessful)
    }
}

fun File.collectFilesWithRelativePaths(baseDir: File): List<Pair<File, String>> {
    val basePathLength = baseDir.parentFile?.absolutePath?.length?.plus(1) ?: 0
    val relativePath = absolutePath.substring(basePathLength).replace(File.separatorChar, '/')
    if (isDirectory) {
        val children = listFiles()?.flatMap { it.collectFilesWithRelativePaths(baseDir) }
            ?: emptyList()
        return listOf(this to "$relativePath/") + children
    }
    return listOf(this to relativePath)
}