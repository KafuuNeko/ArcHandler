package cc.kafuu.archandler.libs.archive.impl.packer

import cc.kafuu.archandler.libs.archive.IPacker
import cc.kafuu.archandler.libs.archive.model.CompressionAlgorithm
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import cc.kafuu.archandler.libs.extensions.commonBaseDir
import cc.kafuu.archandler.libs.jni.NativeCallback
import cc.kafuu.archandler.libs.jni.NativeLib
import cc.kafuu.archandler.libs.jni.model.LibArchiveFormat
import cc.kafuu.archandler.libs.jni.model.LibCompressionType
import java.io.File

class LibArchivePacker(
    private val archiveFile: File,
    private val option: CompressionOption
) : IPacker {
    private fun getFormat() = when (option) {
        is CompressionOption.Cpio -> LibArchiveFormat.Cpio

        is CompressionOption.Tar -> LibArchiveFormat.Tar

        is CompressionOption.Zip -> LibArchiveFormat.Zip

        is CompressionOption.Xar -> LibArchiveFormat.Xar

        else -> throw IllegalArgumentException()
    }

    private fun getCompressionAlgorithm() = when (option) {
        is CompressionOption.Cpio -> option.algorithm
        is CompressionOption.Raw -> option.algorithm
        is CompressionOption.Tar -> option.algorithm
        is CompressionOption.Xar -> option.algorithm
        else -> null
    }

    private fun CompressionAlgorithm.getCompressionType() = when (this) {
        is CompressionAlgorithm.BZip2 -> LibCompressionType.Bzip2
        is CompressionAlgorithm.GZip -> LibCompressionType.Gzip
        is CompressionAlgorithm.Lz4 -> LibCompressionType.Lz4
        is CompressionAlgorithm.Xz -> LibCompressionType.Xz
        is CompressionAlgorithm.Zstd -> LibCompressionType.Zstd
    }

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
        val algorithm = getCompressionAlgorithm()
        return NativeLib.createArchive(
            outputPath = archiveFile.path,
            baseDir = baseDir,
            inputFiles = files.map { it.path },
            format = getFormat().id,
            compression = (algorithm?.getCompressionType() ?: LibCompressionType.None).id,
            compressionLevel = algorithm?.compressionLevel ?: 0,
            listener = nativeListener
        )
    }
}