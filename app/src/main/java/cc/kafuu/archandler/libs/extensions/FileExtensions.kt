package cc.kafuu.archandler.libs.extensions

import cc.kafuu.archandler.libs.archive.ArchiveManager
import cc.kafuu.archandler.libs.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

private val EXTENSION_ICON_MAP = mapOf(
    // image
    FileType.Image to setOf(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".raw", ".svg", ".heif", ".heic"
    ),

    // pdf
    FileType.Pdf to setOf(".pdf"),

    // docs
    FileType.Docs to setOf(
        ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".odp", ".txt", ".rtf",
        ".md"
    ),

    // movie
    FileType.Movie to setOf(
        ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".mpg", ".mpeg", ".3gp", ".ogg"
    ),

    // music
    FileType.Music to setOf(
        ".mp3", ".wav", ".aac", ".flac", ".ogg", ".m4a", ".wma", ".aiff"
    ),

    // database
    FileType.Database to setOf(
        ".db", ".sqlite", ".sqlite3", ".db3", ".mdb", ".accdb"
    )
)

fun File.getFileType(): FileType {
    if (isDirectory) return FileType.Folder
    val extension = name.lowercase()
    for ((icon, extensions) in EXTENSION_ICON_MAP) {
        if (extensions.any { extension.endsWith(it, true) }) {
            return icon
        }
    }
    if (ArchiveManager.isExtractable(this)) return FileType.Archive
    return FileType.Unknow
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

suspend fun File.sha256Of(bufferSize: Int = 1 * 1024 * 1024): String {
    suspend fun digestStreaming(
        input: InputStream,
        md: MessageDigest,
        bufferSize: Int
    ) {
        val buf = ByteArray(bufferSize)
        while (true) {
            kotlin.coroutines.coroutineContext.ensureActive()
            val read = input.read(buf)
            if (read <= 0) break
            md.update(buf, 0, read)
        }
    }
    return withContext(Dispatchers.IO) {
        require(isFile && exists()) { "Not a regular file: $path" }
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(this@sha256Of).use { input ->
            digestStreaming(input, md, bufferSize)
        }
        md.digest().joinToString("") { "%02x".format(it) }
    }
}

fun List<File>.commonBaseDir(): File? {
    if (this.isEmpty()) return null
    val paths = this.map { it.absoluteFile.toPath().normalize().toList() }
    val minLength = paths.minOf { it.size }
    var commonIndex = 0

    loop@ for (i in 0 until minLength) {
        val segment = paths[0][i]
        for (path in paths) {
            if (path[i] != segment) break@loop
        }
        commonIndex++
    }

    return if (commonIndex == 0) {
        null
    } else {
        val segments = paths[0].subList(0, commonIndex)
        val initial = if (segments[0].toString().isEmpty()) File("/") else File(segments[0].toString())
        segments.drop(1).fold(initial) { acc, s ->
            File(acc, s.toString())
        }
    }

}

