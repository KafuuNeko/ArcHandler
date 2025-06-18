package cc.kafuu.archandler.libs.archive

import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import java.io.File

interface IArchive {
    /**
     * 读取压缩包内容
     */
    fun list(dir: String = ""): List<ArchiveEntry>

    /**
     * 提取压缩包某个条目
     */
    fun extract(entry: ArchiveEntry, dest: File)

    /**
     * 解压整个压缩包
     */
    fun extractAll(destDir: File)

    /**
     * 添加文件到压缩包内的destPath下
     */
    fun add(source: File, rootPath: String = "")

    /**
     * 设置压缩包密码提供者
     */
    fun setPasswordProvider(provider: IPasswordProvider)
}