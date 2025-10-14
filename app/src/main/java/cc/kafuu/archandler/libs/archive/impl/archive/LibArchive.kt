package cc.kafuu.archandler.libs.archive.impl.archive

import cc.kafuu.archandler.libs.archive.IArchive
import cc.kafuu.archandler.libs.archive.IPasswordProvider
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.jni.NativeCallback
import cc.kafuu.archandler.libs.jni.NativeLib
import kotlinx.coroutines.runBlocking
import java.io.File


class LibArchive(private val archiveFile: File) : IArchive {
    override suspend fun open(provider: IPasswordProvider?): Boolean = true

    override fun list(dir: String): List<ArchiveEntry> = emptyList()

    override fun extract(
        entry: ArchiveEntry,
        dest: File
    ) = Unit

    override suspend fun extractAll(
        destDir: File,
        onProgress: suspend (Int, String, Int) -> Unit
    ) {
        val nativeListener = object : NativeCallback {
            override fun invoke(vararg args: Any?) {
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

    override fun close() = Unit
}