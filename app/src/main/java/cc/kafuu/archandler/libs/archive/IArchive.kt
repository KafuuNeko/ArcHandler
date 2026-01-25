package cc.kafuu.archandler.libs.archive

import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.archive.model.ArchiveTestResult
import java.io.Closeable
import java.io.File

interface IArchive : Closeable {
    /**
     * 尝试打开压缩包
     */
    suspend fun open(provider: IPasswordProvider? = null): Boolean

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
    suspend fun extractAll(
        destDir: File,
        onProgress: suspend (index: Int, path: String, target: Int) -> Unit = { _, _, _ -> }
    )

    /**
     * 测试归档完整性
     * 验证压缩包是否损坏
     * @return 测试结果，包含是否成功和错误信息
     */
    suspend fun test(): ArchiveTestResult
}