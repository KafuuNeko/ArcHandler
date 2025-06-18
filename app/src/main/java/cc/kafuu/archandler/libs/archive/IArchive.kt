package cc.kafuu.archandler.libs.archive

import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import java.io.Closeable
import java.io.File

interface IArchive : Closeable {
    /**
     * 尝试打开压缩包
     */
    fun open(provider: IPasswordProvider? = null): Boolean

    /**
     * 读取压缩包内容（不递归读取）
     * @param dir 当前压缩包相对路径[dir]下的所有文件或目录
     */
    fun list(dir: String = ""): List<ArchiveEntry>

    /**
     * 提取压缩包某个条目
     */
    fun extract(entry: ArchiveEntry, dest: File)

    /**
     * 解压整个压缩包到指定目录下
     */
    fun extractAll(
        destDir: File,
        onProgress: (index: Int, path: String, target: Int) -> Unit = { _, _, _ -> }
    )
}