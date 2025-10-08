package cc.kafuu.archandler.libs.archive.impl.packer

import cc.kafuu.archandler.libs.archive.IPacker
import cc.kafuu.archandler.libs.extensions.commonBaseDir
import cc.kafuu.archandler.libs.jni.NativeCallback
import cc.kafuu.archandler.libs.jni.NativeLib
import cc.kafuu.archandler.libs.jni.model.LibArchiveFormat
import cc.kafuu.archandler.libs.jni.model.LibCompressionType
import java.io.File

class LibArchiveTarPacker(
    private val archiveFile: File
) : IPacker {
    override suspend fun pack(
        files: List<File>,
        listener: (Int, Int, String) -> Unit
    ): Boolean {
        val nativeListener = object : NativeCallback {
            override fun invoke(vararg args: Any?) {
                val path = (args[0] as? String) ?: ""
                val index = (args[1] as? Int) ?: 0
                val total = (args[2] as? Int) ?: 0
                listener(index, total, path)
            }
        }
        var baseDir = files.commonBaseDir()?.path ?: return false
        if (!baseDir.startsWith("/")) baseDir = "/$baseDir"
        return NativeLib.createArchive(
            outputPath = archiveFile.path,
            baseDir = baseDir,
            inputFiles = files.map { it.path },
            format = LibArchiveFormat.Tar.id,
            compression = LibCompressionType.None.id,
            compressionLevel = 0,
            listener = nativeListener
        )
    }
}