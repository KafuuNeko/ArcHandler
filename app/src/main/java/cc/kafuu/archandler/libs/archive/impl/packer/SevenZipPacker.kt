package cc.kafuu.archandler.libs.archive.impl.packer

import cc.kafuu.archandler.libs.archive.IPacker
import cc.kafuu.archandler.libs.archive.model.CompressionAlgorithm
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import cc.kafuu.archandler.libs.extensions.collectFilesWithRelativePaths
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import net.sf.sevenzipjbinding.ICryptoGetTextPassword
import net.sf.sevenzipjbinding.IOutCreateArchive
import net.sf.sevenzipjbinding.IOutCreateCallback
import net.sf.sevenzipjbinding.IOutFeatureSetEncryptHeader
import net.sf.sevenzipjbinding.IOutItem7z
import net.sf.sevenzipjbinding.IOutItemBZip2
import net.sf.sevenzipjbinding.IOutItemBase
import net.sf.sevenzipjbinding.IOutItemGZip
import net.sf.sevenzipjbinding.IOutItemTar
import net.sf.sevenzipjbinding.IOutItemZip
import net.sf.sevenzipjbinding.ISequentialInStream
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.OutItemFactory
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import java.io.File
import java.io.RandomAccessFile
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

class SevenZipPacker(
    private val archiveFile: File,
    private val option: CompressionOption
) : IPacker {

    /**
     * 创建 OutArchive
     */
    private fun createOutArchive() = when (option) {
        is CompressionOption.Tar -> SevenZip.openOutArchiveTar()

        is CompressionOption.SevenZip -> SevenZip.openOutArchive7z().apply {
            if (option.password != null && this is IOutFeatureSetEncryptHeader) {
                setHeaderEncryption(true)
            }
            setLevel(option.compressionLevel)
        }

        is CompressionOption.Zip -> SevenZip.openOutArchiveZip().apply {
            if (option.password != null && this is IOutFeatureSetEncryptHeader) {
                setHeaderEncryption(true)
            }
            setLevel(option.compressionLevel)
        }

        else -> throw IllegalArgumentException()
    }

    /**
     * 获取压缩包密码
     */
    private fun getPassword() = when (option) {
        is CompressionOption.SevenZip -> option.password
        is CompressionOption.Zip -> option.password
        else -> null
    }

    /**
     * 创建打包回调
     */
    private fun <I : IOutItemBase> createCallback(
        onGetItemInformation: (index: Int, itemFactory: OutItemFactory<I?>?) -> I?,
        onGetStream: (index: Int) -> ISequentialInStream?,
        onSetTotal: (total: Long) -> Unit = {},
        onSetCompleted: (completed: Long) -> Unit = {}
    ): IOutCreateCallback<I> {
        val password = getPassword()
        return if (password != null) {
            object : IOutCreateCallback<I>, ICryptoGetTextPassword {
                override fun cryptoGetTextPassword(): String = password

                override fun getItemInformation(
                    index: Int,
                    item: OutItemFactory<I?>?
                ) = onGetItemInformation(index, item)

                override fun getStream(index: Int) = onGetStream(index)

                override fun setTotal(total: Long) = onSetTotal(total)

                override fun setCompleted(complete: Long) = onSetCompleted(complete)

                override fun setOperationResult(ok: Boolean) = Unit
            }
        } else {
            object : IOutCreateCallback<I> {
                override fun getItemInformation(
                    index: Int,
                    item: OutItemFactory<I?>?
                ) = onGetItemInformation(index, item)

                override fun getStream(index: Int) = onGetStream(index)

                override fun setTotal(total: Long) = onSetTotal(total)

                override fun setCompleted(complete: Long) = onSetCompleted(complete)

                override fun setOperationResult(ok: Boolean) = Unit
            }
        }
    }

    private fun createCallback(
        allEntries: List<Pair<File, String>>,
        onGetStream: (Int) -> ISequentialInStream?
    ) = when (option) {
        is CompressionOption.Raw -> when (option.algorithm) {
            is CompressionAlgorithm.BZip2 -> createCallback<IOutItemBZip2>(
                onGetItemInformation = { index, itemFactory ->
                    itemFactory?.createOutItem()
                },
                onGetStream = onGetStream
            )

            is CompressionAlgorithm.GZip -> createCallback<IOutItemGZip>(
                onGetItemInformation = { index, itemFactory ->
                    val (_, relativePath) = allEntries[index]
                    itemFactory?.createOutItem()?.apply {
                        propertyPath = relativePath
                    }
                },
                onGetStream = onGetStream
            )

            else -> throw IllegalArgumentException()
        }

        is CompressionOption.Tar -> createCallback<IOutItemTar>(
            onGetItemInformation = { index, itemFactory ->
                val (file, relativePath) = allEntries[index]
                itemFactory?.createOutItem()?.apply {
                    propertyPath = relativePath
                    propertyIsDir = file.isDirectory
                }
            },
            onGetStream = onGetStream
        )

        is CompressionOption.SevenZip -> createCallback<IOutItem7z>(
            onGetItemInformation = { index, itemFactory ->
                val (file, relativePath) = allEntries[index]
                itemFactory?.createOutItem()?.apply {
                    propertyPath = relativePath
                    propertyIsDir = file.isDirectory
                    if (!file.isDirectory) dataSize = file.length()
                }
            },
            onGetStream = onGetStream
        )

        is CompressionOption.Zip -> createCallback<IOutItemZip>(
            onGetItemInformation = { index, itemFactory ->
                val (file, relativePath) = allEntries[index]
                itemFactory?.createOutItem()?.apply {
                    propertyPath = relativePath
                    propertyIsDir = file.isDirectory
                }
            },
            onGetStream = onGetStream
        )

        else -> throw IllegalArgumentException()
    }

    /**
     * 创建 Archive
     */
    private suspend fun createArchive(
        outStream: ISequentialOutStream,
        sourceFiles: List<File>,
        listener: (current: Int, total: Int, filePath: String) -> Unit
    ) {
        createOutArchive().use { archive ->
            val allEntries = sourceFiles.flatMap { file ->
                file.collectFilesWithRelativePaths(file)
            }
            coroutineContext.ensureActive()
            val ctx = coroutineContext
            val total = allEntries.size
            val callback = createCallback(allEntries) { index ->
                if (!ctx.isActive) throw CancellationException()
                val (file, _) = allEntries[index]
                listener(index + 1, total, file.absolutePath)
                if (!file.isFile) {
                    null
                } else {
                    RandomAccessFileInStream(RandomAccessFile(file, "r"))
                }
            }
            (archive as IOutCreateArchive<IOutItemBase>).createArchive(outStream, total, callback)
        }
    }

    /**
     * 执行打包
     */
    override suspend fun pack(
        files: List<File>,
        listener: (current: Int, total: Int, filePath: String) -> Unit
    ) = try {
        coroutineContext.ensureActive()
        RandomAccessFile(archiveFile, "rw").use { raf ->
            createArchive(RandomAccessFileOutStream(raf), files, listener)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
