package cc.kafuu.archandler.libs.archive

import cc.kafuu.archandler.libs.archive.impl.SevenZipArchive
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import net.sf.sevenzipjbinding.ArchiveFormat
import java.io.File

class ArchiveManager {
    /**
     * 判断是否是可操作的压缩包
     */
    fun isArchive(file: File): Boolean {
        return listOf("zip", "7z").contains(file.extension.lowercase())
    }

    /**
     * 尝试打开一个压缩包
     */
    fun openArchive(file: File): IArchive? {
        return when (file.extension.lowercase()) {
            "zip" -> SevenZipArchive(file, ArchiveFormat.ZIP)
            "7z" -> SevenZipArchive(file, ArchiveFormat.SEVEN_ZIP)
//                "tar", "gz", "bz2" -> LibArchiveWrapper(file)
            else -> null
        }
    }

    /**
     * 准备开始打包
     * @param archiveFile 压缩包文件
     * @param sourceFiles 将要被打包的文件或目录列表
     * @param compressionOption 要执行的打包操作类型
     */
    fun createPacker(
        archiveFile: File,
        sourceFiles: List<File>,
        compressionOption: CompressionOption
    ): IPacker {
        TODO("")
//        return when (compressionOption) {
//            is CompressionOption.Zip -> ZipPacker(archiveFile, sourceFiles, compressionOption)
//            is CompressionOption.SevenZip -> SevenZipPacker(
//                archiveFile,
//                sourceFiles,
//                compressionOption
//            )
//
//            is CompressionOption.Tar,
//            is CompressionOption.GZip,
//            is CompressionOption.BZip2 -> TarLikePacker(archiveFile, sourceFiles, compressionOption)
//        }
    }
}