package cc.kafuu.archandler.libs.archive.impl.archive

import cc.kafuu.archandler.libs.archive.IArchive
import cc.kafuu.archandler.libs.archive.IPasswordProvider
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.manager.CacheManager
import cc.kafuu.archandler.libs.model.AppCacheType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class SevenZipArchive(
    private val archiveFile: File,
    private val format: ArchiveFormat
) : IArchive, KoinComponent {
    private var mArchive: IInArchive? = null
    private var mSimpleArchive: ISimpleInArchive? = null
    private var mPassword: String? = null
    private val mCacheManager by inject<CacheManager>()

    // 缓存文件集
    private var mCacheFiles: MutableSet<File> = mutableSetOf()

    /**
     * 打开压缩包
     */
    override suspend fun open(provider: IPasswordProvider?): Boolean {
        val fileToOpen = prepareArchiveFile()

        if (tryOpen(fileToOpen)) {
            if (hasEncryptedEntries()) {
                mPassword = provider?.getPassword(archiveFile) ?: return false
                if (!tryOpen(fileToOpen, mPassword)) return false
            }
        } else {
            mPassword = provider?.getPassword(archiveFile) ?: return false
            if (!tryOpen(fileToOpen, mPassword)) return false
        }
        return mArchive != null && mSimpleArchive != null
    }

    /**
     * 判断是否是分卷文件，如果是则合并为临时文件返回
     * 否则直接返回原始文件
     */
    private suspend fun prepareArchiveFile(): File {
        val name = archiveFile.name.lowercase()
        if (name.matches(Regex(""".*\.(zip|7z)\.001$"""))) {
            val baseName = archiveFile.nameWithoutExtension.substringBeforeLast('.')
            val dir = archiveFile.parentFile ?: return archiveFile
            val partFiles = dir.listFiles()
                ?.filter { it.name.startsWith(baseName) && it.name.matches(Regex(""".*\.(zip|7z)\.\d{3}$""")) }
                ?.sortedBy { it.name } ?: return archiveFile

            // 创建缓存文件并合并
            val merged = mCacheManager.createCacheFile(AppCacheType.MERGE_SPLIT_ARCHIVE, ".$format")
            mCacheFiles.add(merged)

            withContext(Dispatchers.IO) {
                merged.outputStream().use { out ->
                    partFiles.forEach { part ->
                        part.inputStream().use { input -> input.copyTo(out) }
                    }
                }
            }
            return merged
        }

        return archiveFile
    }

    /**
     * 尝试打开压缩包
     */
    private fun tryOpen(file: File, pwd: String? = null): Boolean = try {
        val raf = RandomAccessFile(file, "r")
        val inStream = RandomAccessFileInStream(raf)
        mArchive = SevenZip.openInArchive(format, inStream, pwd)
        mSimpleArchive = mArchive?.simpleInterface
        true
    } catch (e: SevenZipException) {
        e.printStackTrace()
        false
    }

    /**
     * 判断此压缩包是否是加密的
     */
    private fun hasEncryptedEntries() =
        mSimpleArchive?.archiveItems?.any { it.isEncrypted } == true

    /**
     * 列出压缩包内所有内容
     */
    override fun list(dir: String): List<ArchiveEntry> =
        mSimpleArchive?.archiveItems?.map {
            ArchiveEntry(
                path = it.path ?: "",
                name = File(it.path ?: "").name,
                isDirectory = it.isFolder,
                size = it.size,
                compressedSize = it.packedSize,
                lastModified = runCatching { it.lastWriteTime.time }.getOrNull() ?: 0L
            )
        } ?: emptyList()

    /**
     * 提取压缩包指定内容
     */
    override fun extract(entry: ArchiveEntry, dest: File) {
        val item = mSimpleArchive?.archiveItems?.firstOrNull { it.path == entry.path } ?: return
        File(dest, entry.name).also { it.parentFile?.mkdirs() }.let { fos ->
            FileOutputStream(fos)
        }.use { out ->
            item.extractSlow({ data ->
                out.write(data)
                data.size
            }, mPassword)
        }
    }

    /**
     * 提取压缩包所有内容
     */
    override fun extractAll(
        destDir: File,
        onProgress: (index: Int, path: String, target: Int) -> Unit
    ) {
        var index = 0
        val target = mSimpleArchive?.archiveItems
            ?.count { it.path != null && !it.isFolder } ?: 0

        mSimpleArchive?.archiveItems?.forEach { item ->
            val path = item.path ?: return@forEach
            val outFile = File(destDir, path)
            if (item.isFolder) {
                outFile.mkdirs()
                return@forEach
            }
            onProgress(index, path, target)
            outFile.parentFile?.mkdirs()
            FileOutputStream(outFile).use { out ->
                item.extractSlow({ data ->
                    out.write(data)
                    data.size
                }, mPassword)
            }
            index++
        }
    }

    override fun close() {
        runCatching {
            mArchive?.close()
            for (file in mCacheFiles) {
                file.delete()
            }
            mCacheFiles.clear()
        }
    }
}