package cc.kafuu.archandler.libs.archive.impl.archive

import android.util.Log
import cc.kafuu.archandler.libs.archive.IArchive
import cc.kafuu.archandler.libs.archive.IPasswordProvider
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.archive.model.ArchiveTestResult
import cc.kafuu.archandler.libs.jni.NativeCallback
import cc.kafuu.archandler.libs.jni.NativeLib
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.coroutines.cancellation.CancellationException


class LibArchive(private val archiveFile: File) : IArchive {
    override suspend fun open(provider: IPasswordProvider?): Boolean = true

    override fun list(dir: String): List<ArchiveEntry> {
        val files = NativeLib.fetchArchiveFiles(archiveFile.path)?.toList() ?: emptyList()
        return files.sortedBy { it.path }
    }

    override fun extract(
        entry: ArchiveEntry,
        dest: File
    ) = Unit

    override suspend fun extractAll(
        destDir: File,
        onProgress: suspend (Int, String, Int) -> Unit
    ) {
        val ctx = currentCoroutineContext()
        val nativeListener = object : NativeCallback {
            override fun invoke(vararg args: Any?) {
                // 检查协程是否仍然活跃，如果已取消则抛出异常
                if (!ctx.isActive) {
                    throw CancellationException("Archive extraction cancelled")
                }
                val path = (args[0] as? String) ?: ""
                val index = (args[1] as? Int) ?: 0
                val total = (args[2] as? Int) ?: 0
                runBlocking { onProgress(index, path, total) }
            }
        }
        NativeLib.extractArchive(
            archiveFile.path,
            destDir.path,
            nativeListener
        )
    }

    override suspend fun test(): ArchiveTestResult {
        return try {
            val files = list()
            val fileCount = files.count { !it.isDirectory }
            ArchiveTestResult.success(testedFiles = fileCount, totalFiles = fileCount)
        } catch (e: Exception) {
            ArchiveTestResult.error(e.message ?: "Failed to test archive")
        }
    }

    override fun close() = Unit
}