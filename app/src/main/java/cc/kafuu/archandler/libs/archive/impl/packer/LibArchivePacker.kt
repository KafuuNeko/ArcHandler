package cc.kafuu.archandler.libs.archive.impl.packer

import cc.kafuu.archandler.libs.archive.IPacker
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
        is CompressionOption.Cpio,
        is CompressionOption.CpioBzip2,
        is CompressionOption.CpioGZip,
        is CompressionOption.CpioXz,
        is CompressionOption.CpioLz4,
        is CompressionOption.CpioZstd -> LibArchiveFormat.Cpio

        is CompressionOption.Tar,
        is CompressionOption.TarBzip2,
        is CompressionOption.TarGZip,
        is CompressionOption.TarXz,
        is CompressionOption.TarLz4,
        is CompressionOption.TarZstd -> LibArchiveFormat.Tar

        is CompressionOption.Zip -> LibArchiveFormat.Zip

        else -> throw IllegalArgumentException()
    }

    private fun getCompression() = when (option) {
        is CompressionOption.Tar,
        is CompressionOption.Cpio,
        is CompressionOption.Zip -> LibCompressionType.None

        is CompressionOption.TarBzip2,
        is CompressionOption.CpioBzip2 -> LibCompressionType.Bzip2

        is CompressionOption.CpioGZip,
        is CompressionOption.TarGZip -> LibCompressionType.Gzip

        is CompressionOption.CpioXz,
        is CompressionOption.TarXz -> LibCompressionType.Xz

        is CompressionOption.CpioLz4,
        is CompressionOption.TarLz4 -> LibCompressionType.Lz4

        is CompressionOption.CpioZstd,
        is CompressionOption.TarZstd -> LibCompressionType.Zstd

        else -> throw IllegalArgumentException()
    }

    private fun getCompressionLevel() = when (option) {
        is CompressionOption.BZip2 -> option.compressionLevel
        is CompressionOption.CpioBzip2 -> option.compressionLevel
        is CompressionOption.CpioGZip -> option.compressionLevel
        is CompressionOption.CpioXz -> option.compressionLevel
        is CompressionOption.CpioZstd -> option.compressionLevel
        is CompressionOption.GZip -> option.compressionLevel
        is CompressionOption.SevenZip -> option.compressionLevel
        is CompressionOption.TarBzip2 -> option.compressionLevel
        is CompressionOption.TarGZip -> option.compressionLevel
        is CompressionOption.TarXz -> option.compressionLevel
        is CompressionOption.TarZstd -> option.compressionLevel
        is CompressionOption.Zip -> option.compressionLevel
        else -> 0
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
        return NativeLib.createArchive(
            outputPath = archiveFile.path,
            baseDir = baseDir,
            inputFiles = files.map { it.path },
            format = getFormat().id,
            compression = getCompression().id,
            compressionLevel = getCompressionLevel(),
            listener = nativeListener
        )
    }
}