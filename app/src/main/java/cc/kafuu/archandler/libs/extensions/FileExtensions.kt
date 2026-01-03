package cc.kafuu.archandler.libs.extensions

import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.archive.ArchiveManager
import cc.kafuu.archandler.libs.model.FileConflictStrategy
import cc.kafuu.archandler.libs.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
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

fun File.generateUniqueFile(targetDir: File): File {
    var targetFile = File(targetDir, name)
    var index = 0
    val baseName = nameWithoutExtension
    val ext = extension

    while (targetFile.exists()) {
        val newName = if (ext.isNotEmpty()) {
            "${baseName}(${++index}).$ext"
        } else {
            "${baseName}(${++index})"
        }
        targetFile = File(targetDir, newName)
    }

    return targetFile
}

fun File.getSameNameDirectory(): File {
    val name = if (this.isFile) {
        this.nameWithoutExtension
    } else {
        this.name
    }
    return File(this.parentFile, name)
}

/**
 * 优化的删除函数，支持协程和基于时间的批量更新
 * @param onProgressUpdate 进度更新回调，参数为 (已删除文件数, 总文件数)
 * @param updateIntervalMs 更新间隔时间（毫秒），默认 200ms
 */
suspend fun List<File>.deletes(
    onProgressUpdate: ((deletedCount: Int, totalCount: Int) -> Unit)? = null,
    updateIntervalMs: Long = 200
) = withContext(Dispatchers.IO) {
    var deletedCount = 0
    val filesToDelete = mutableListOf<File>()
    val directoriesToDelete = mutableListOf<File>()
    
    // 使用迭代方式收集所有需要删除的文件（避免递归栈溢出）
    val stack = mutableListOf<File>()
    stack.addAll(this@deletes)
    
    while (stack.isNotEmpty()) {
        currentCoroutineContext().ensureActive()
        val file = stack.removeAt(stack.size - 1)
        
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                stack.add(child)
            }
            directoriesToDelete.add(file)
        } else {
            filesToDelete.add(file)
        }
    }
    
    // 先删除所有文件，再删除目录
    val allFilesToDelete = filesToDelete + directoriesToDelete.reversed()
    val totalFiles = allFilesToDelete.size
    
    // 初始化进度更新
    if (onProgressUpdate != null && totalFiles > 0) {
        withContext(Dispatchers.Main) {
            onProgressUpdate(0, totalFiles)
        }
    }
    
    var lastUpdateTime = System.currentTimeMillis()
    
    // 批量删除文件
    for ((index, file) in allFilesToDelete.withIndex()) {
        currentCoroutineContext().ensureActive()
        
        val isSuccessful = try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        
        if (isSuccessful) deletedCount++
        
        // 基于时间间隔更新进度（减少 UI 更新频率）
        val currentTime = System.currentTimeMillis()
        if (onProgressUpdate != null && 
            (currentTime - lastUpdateTime >= updateIntervalMs || index == totalFiles - 1)) {
            withContext(Dispatchers.Main) {
                onProgressUpdate(deletedCount, totalFiles)
            }
            lastUpdateTime = currentTime
        }
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
            currentCoroutineContext().ensureActive()
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

    if (this.size == 1) return this[0].parentFile

    val paths: List<List<String>> = this.map { file ->
        val p = try {
            file.absoluteFile.canonicalPath
        } catch (_: IOException) {
            file.absoluteFile.absolutePath
        }

        val sep = File.separatorChar
        val raw = p.split(sep)
        val segments = mutableListOf<String>()
        if (p.startsWith(File.separator)) {
            segments.add(File.separator)
        }
        for (seg in raw) {
            if (seg.isNotEmpty()) segments.add(seg)
        }
        segments
    }

    val minLength = paths.minOf { it.size }
    var commonIndex = 0

    loop@ for (i in 0 until minLength) {
        val segment = paths[0][i]
        for (path in paths) {
            if (path[i] != segment) break@loop
        }
        commonIndex++
    }

    if (commonIndex == 0) return null

    val commonSegments = paths[0].subList(0, commonIndex)

    val result = if (commonSegments[0] == File.separator) {
        if (commonSegments.size == 1) {
            File(File.separator)
        } else {
            File(commonSegments.drop(1).joinToString(File.separator, prefix = File.separator))
        }
    } else {
        File(commonSegments.joinToString(File.separator))
    }

    return result
}

fun File.listFilteredFiles(
    showHiddenFiles: Boolean = AppModel.isShowHiddenFiles,
    showUnreadableDirectories: Boolean = AppModel.isShowUnreadableDirectories,
    showUnreadableFiles: Boolean = AppModel.isShowUnreadableFiles
): List<File> {
    return this.listFiles()
        ?.asList()
        ?.filter { file ->
            file != null &&
                    (showHiddenFiles || !file.name.startsWith(".")) &&
                    (showUnreadableDirectories || !file.isDirectory || file.canRead()) &&
                    (showUnreadableFiles || !file.isFile || file.canRead())
        } ?: emptyList()
}

fun File.copyFileCompat(dst: File): Boolean = try {
    inputStream().use { input ->
        dst.outputStream().use { output -> input.copyTo(output) }
    }
    true
} catch (_: Exception) {
    false
}

fun File.moveWithFallback(dst: File): Boolean {
    try {
        if (renameTo(dst)) return true
    } catch (_: Exception) {
    }
    try {
        if (dst.exists()) dst.delete()
        inputStream().use { input -> dst.outputStream().use { output -> input.copyTo(output) } }
        if (!delete()) {
            dst.delete()
            return false
        }
        return true
    } catch (_: Exception) {
        return false
    }
}

suspend fun File.copyOrMoveTo(
    target: File,
    isMove: Boolean,
    onStart: suspend (srcFile: File, finalTargetFile: File) -> Unit = { _, _ -> },
    onConflict: suspend (srcFile: File, targetFile: File) -> FileConflictStrategy = { _, _ -> FileConflictStrategy.Skip },
    onSuccess: suspend (srcFile: File, finalTargetFile: File, strategy: FileConflictStrategy) -> Unit = { _, _, _ -> }
): Boolean {
    val actualTarget = if (target.isDirectory) File(target, name) else target
    currentCoroutineContext().ensureActive()
    // 处理文件
    if (isFile) {
        onStart(this, actualTarget)

        // 是否存在冲突
        val strategy = if (actualTarget.exists()) {
            onConflict(this, actualTarget)
        } else {
            FileConflictStrategy.Overwrite
        }

        // 处理最终目标
        val finalTarget = when (strategy) {
            FileConflictStrategy.Skip -> actualTarget
            FileConflictStrategy.Overwrite -> actualTarget
            FileConflictStrategy.KeepBoth -> this.generateUniqueFile(actualTarget.parentFile!!)
        }

        if (strategy == FileConflictStrategy.Skip) {
            onSuccess(this, finalTarget, strategy)
            return true
        }

        // 执行 copy 或 move
        val success = withContext(Dispatchers.IO) {
            if (isMove) {
                this@copyOrMoveTo.moveWithFallback(finalTarget)
            } else {
                this@copyOrMoveTo.copyFileCompat(finalTarget)
            }
        }

        if (success) onSuccess(this, finalTarget, strategy)
        return success
    }
    if (isDirectory) {
        actualTarget.mkdirs()
        listFiles()?.forEach { child ->
            if (!child.copyOrMoveTo(actualTarget, isMove, onStart, onConflict, onSuccess)) {
                return false
            }
        }
        if (isMove) delete()
        return true
    }
    return false
}

suspend fun List<File>.copyOrMoveTo(
    target: File,
    isMove: Boolean,
    onStart: suspend (srcFile: File, finalTargetFile: File) -> Unit = { _, _ -> },
    onConflict: suspend (srcFile: File, targetFile: File) -> FileConflictStrategy = { _, _ -> FileConflictStrategy.Skip },
    onSuccess: suspend (srcFile: File, finalTargetFile: File, strategy: FileConflictStrategy) -> Unit = { _, _, _ -> },
): Boolean = all { it.copyOrMoveTo(target, isMove, onStart, onConflict, onSuccess) }

fun File.hasUnmovableItems(): Boolean {
    if (!exists()) return true
    if (!canRead() || !canWrite()) return true
    if (isFile) return false
    if (isDirectory) {
        val children = listFiles() ?: return true
        for (child in children) {
            if (child.hasUnmovableItems()) return true
        }
        return false
    }
    return true
}

fun List<File>.hasUnmovableItems(): Boolean = all { it.hasUnmovableItems() }

fun File.countAllFiles(): Int {
    if (isFile) return 1
    if (!isDirectory) return 0
    var count = 0
    listFiles()?.forEach { child ->
        count += child.countAllFiles()
    }
    return count
}