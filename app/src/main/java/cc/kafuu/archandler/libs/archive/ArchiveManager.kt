package cc.kafuu.archandler.libs.archive

import cc.kafuu.archandler.libs.archive.impl.archive.SevenZipArchive
import cc.kafuu.archandler.libs.archive.impl.packer.LibArchivePacker
import cc.kafuu.archandler.libs.archive.impl.packer.SevenZipPacker
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import net.sf.sevenzipjbinding.ArchiveFormat
import java.io.File

class ArchiveManager {
    companion object {
        /**
         * 判断是否可解压
         */
        fun isExtractable(file: File): Boolean {
            val supportedExtensions = setOf(
                "zip", "tar", "split", "rar", "rar5",
                "lzma", "iso", "hfs", "gzip", "gz",
                "cpio", "bzip2", "bz2", "7z", "z",
                "arj", "cab", "lzh", "chm", "nsis",
                "ar", "rpm", "udf", "wim", "xar",
                "fat", "ntfs"
            )
            val name = file.name.lowercase()
            if (Regex(""".*\.(7z|zip)\.\d{3}$""").matches(name)) return true
            return supportedExtensions.contains(file.extension.lowercase())
        }
    }

    /**
     * 尝试打开一个压缩包
     */
    fun openArchive(file: File): IArchive? {
        val name = file.name.lowercase()
        return when {
            Regex(""".*\.7z\.\d{3}$""").matches(name) -> SevenZipArchive(
                file,
                ArchiveFormat.SEVEN_ZIP
            )

            Regex(""".*\.zip\.\d{3}$""").matches(name) -> SevenZipArchive(file, ArchiveFormat.ZIP)
            else -> when (file.extension.lowercase()) {
                "zip" -> SevenZipArchive(file, ArchiveFormat.ZIP)
                "tar" -> SevenZipArchive(file, ArchiveFormat.TAR)
                "split" -> SevenZipArchive(file, ArchiveFormat.SPLIT)
                "rar" -> SevenZipArchive(file, ArchiveFormat.RAR)
                "rar5" -> SevenZipArchive(file, ArchiveFormat.RAR5)
                "lzma" -> SevenZipArchive(file, ArchiveFormat.LZMA)
                "iso" -> SevenZipArchive(file, ArchiveFormat.ISO)
                "hfs" -> SevenZipArchive(file, ArchiveFormat.HFS)
                "gzip", "gz" -> SevenZipArchive(file, ArchiveFormat.GZIP)
                "cpio" -> SevenZipArchive(file, ArchiveFormat.CPIO)
                "bzip2", "bz2" -> SevenZipArchive(file, ArchiveFormat.BZIP2)
                "7z" -> SevenZipArchive(file, ArchiveFormat.SEVEN_ZIP)
                "z" -> SevenZipArchive(file, ArchiveFormat.Z)
                "arj" -> SevenZipArchive(file, ArchiveFormat.ARJ)
                "cab" -> SevenZipArchive(file, ArchiveFormat.CAB)
                "lzh" -> SevenZipArchive(file, ArchiveFormat.LZH)
                "chm" -> SevenZipArchive(file, ArchiveFormat.CHM)
                "nsis" -> SevenZipArchive(file, ArchiveFormat.NSIS)
                "ar" -> SevenZipArchive(file, ArchiveFormat.AR)
                "rpm" -> SevenZipArchive(file, ArchiveFormat.RPM)
                "udf" -> SevenZipArchive(file, ArchiveFormat.UDF)
                "wim" -> SevenZipArchive(file, ArchiveFormat.WIM)
                "xar" -> SevenZipArchive(file, ArchiveFormat.XAR)
                "fat" -> SevenZipArchive(file, ArchiveFormat.FAT)
                "ntfs" -> SevenZipArchive(file, ArchiveFormat.NTFS)
                else -> null
            }
        }
    }

    /**
     * 准备开始打包
     * @param archiveFile 压缩包文件
     * @param compressionOption 要执行的打包操作
     */
    fun createPacker(
        archiveFile: File,
        compressionOption: CompressionOption
    ): IPacker {
        return when (compressionOption) {
            is CompressionOption.Tar,
            is CompressionOption.GZip,
            is CompressionOption.BZip2,
            is CompressionOption.Zip,
            is CompressionOption.SevenZip -> SevenZipPacker(archiveFile, compressionOption)

            is CompressionOption.TarBzip2,
            is CompressionOption.TarGZip,
            is CompressionOption.TarXz,
            is CompressionOption.Cpio,
            is CompressionOption.CpioBzip2,
            is CompressionOption.CpioGZip,
            is CompressionOption.CpioXz -> LibArchivePacker(archiveFile, compressionOption)
        }
    }
}