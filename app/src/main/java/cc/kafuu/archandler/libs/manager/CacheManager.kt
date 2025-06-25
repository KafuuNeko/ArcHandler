package cc.kafuu.archandler.libs.manager

import android.content.Context
import cc.kafuu.archandler.libs.model.AppCacheType
import java.io.File
import java.util.UUID

class CacheManager(context: Context) {
    private val mCacheRoot: File = context.cacheDir

    /**
     * 获取指定类型的缓存子目录，不存在则创建
     */
    private fun getCacheDir(type: AppCacheType): File {
        val subDir = File(mCacheRoot, type.subDirName)
        if (!subDir.exists()) subDir.mkdirs()
        return subDir
    }

    /**
     * 创建一个缓存文件，文件名随机生成，属于指定类型子目录中
     */
    fun createCacheFile(type: AppCacheType, suffix: String = ".tmp"): File {
        val dir = getCacheDir(type)
        while (true) {
            val fileName = UUID.randomUUID().toString() + suffix
            val file = File(dir, fileName)
            if (!file.exists()) {
                file.createNewFile()
                return file
            }
        }
    }

    /**
     * 列出指定缓存类型下所有普通文件（不含子目录）
     */
    fun listCacheFiles(type: AppCacheType): List<File> {
        return getCacheDir(type).listFiles()?.filter { it.isFile } ?: emptyList()
    }

    /**
     * 清除指定缓存类型的所有文件
     */
    fun clearCache(type: AppCacheType) {
        listCacheFiles(type).forEach { it.delete() }
    }

    /**
     * 清除所有类型的缓存（所有子目录）
     */
    fun clearAllCache() {
        AppCacheType.entries.forEach { clearCache(it) }
    }

    /**
     * 获取指定类型下的缓存文件（如果存在）
     */
    fun getCacheFile(type: AppCacheType, fileName: String): File? {
        val file = File(getCacheDir(type), fileName)
        return if (file.exists() && file.isFile) file else null
    }
}
