package cc.kafuu.archandler.libs.archive.impl

import cc.kafuu.archandler.libs.archive.IArchive
import cc.kafuu.archandler.libs.archive.IPasswordProvider
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchive
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class SevenZipArchive(
    private val archiveFile: File,
    private val format: ArchiveFormat
) : IArchive {
    private var mArchive: IInArchive? = null
    private var mSimpleArchive: ISimpleInArchive? = null
    private var mPassword: String? = null

    override suspend fun open(provider: IPasswordProvider?): Boolean {
        if (tryOpen()) {
            if (hasEncryptedEntries()) {
                mPassword = provider?.getPassword(archiveFile) ?: return false
                if (!tryOpen(mPassword)) return false
            }
        } else {
            mPassword = provider?.getPassword(archiveFile) ?: return false
            if (!tryOpen(mPassword)) return false
        }
        return mArchive != null && mSimpleArchive != null
    }

    private fun tryOpen(pwd: String? = null): Boolean = try {
        val raf = RandomAccessFile(archiveFile, "r")
        val inStream = RandomAccessFileInStream(raf)
        mArchive = SevenZip.openInArchive(format, inStream, pwd)
        mSimpleArchive = mArchive?.simpleInterface
        true
    } catch (e: SevenZipException) {
        e.printStackTrace()
        false
    }

    private fun hasEncryptedEntries() =
        mSimpleArchive?.archiveItems?.any { it.isEncrypted } == true

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
        runCatching { mArchive?.close() }
    }
}
