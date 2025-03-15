package cc.kafuu.archandler.libs.utils

import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

object TestUtils {
    fun buildStorageDataList(size: Int = 10): List<StorageData> {
        return (0..size).map { StorageData("Storage$it", File("")) }
    }

    fun buildFileList(size: Int = 10): List<File> {
        val list = listOf(".txt", ".docx", ".xlsx", ".png", ".zip")
        return (0..size).map {
            File("/test/data/File$it${list[(Random.nextInt(1..list.size) - 1)]}")
        }
    }
}