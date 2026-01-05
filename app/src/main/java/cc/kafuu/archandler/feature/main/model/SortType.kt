package cc.kafuu.archandler.feature.main.model

import android.content.Context
import cc.kafuu.archandler.R
import java.io.File

/**
 * 文件排序类型枚举
 */
enum class SortType(val value: Int) {
    DATE_NEW_TO_OLD(0),
    DATE_OLD_TO_NEW(1),
    SIZE_BIG_TO_SMALL(2),
    SIZE_SMALL_TO_BIG(3),
    NAME_A_TO_Z(4),
    NAME_Z_TO_A(5);

    companion object {
        fun fromValue(value: Int): SortType {
            return entries.firstOrNull { it.value == value } ?: DATE_NEW_TO_OLD
        }
    }

    /**
     * 获取排序类型的显示名称
     */
    fun getTitle(context: Context): String {
        return when (this) {
            DATE_NEW_TO_OLD -> context.getString(R.string.date_new_old)
            DATE_OLD_TO_NEW -> context.getString(R.string.date_old_new)
            SIZE_BIG_TO_SMALL -> context.getString(R.string.size_big_small)
            SIZE_SMALL_TO_BIG -> context.getString(R.string.size_small_big)
            NAME_A_TO_Z -> context.getString(R.string.name_a_z)
            NAME_Z_TO_A -> context.getString(R.string.name_z_a)
        }
    }

    /**
     * 创建比较器
     */
    fun createComparator(): Comparator<File> = Comparator { file1, file2 ->
        when {
            file1.isDirectory && !file2.isDirectory -> -1
            !file1.isDirectory && file2.isDirectory -> 1
            else -> when (this) {
                DATE_NEW_TO_OLD -> compareByDate(file1, file2, false)
                DATE_OLD_TO_NEW -> compareByDate(file1, file2, true)
                SIZE_BIG_TO_SMALL -> compareBySize(file1, file2, false)
                SIZE_SMALL_TO_BIG -> compareBySize(file1, file2, true)
                NAME_A_TO_Z -> compareByName(file1, file2, true)
                NAME_Z_TO_A -> compareByName(file1, file2, false)
            }
        }
    }

    private fun compareByDate(file1: File, file2: File, ascending: Boolean): Int {
        val result = file1.lastModified().compareTo(file2.lastModified())
        return if (ascending) result else -result
    }

    private fun compareBySize(file1: File, file2: File, ascending: Boolean): Int {
        val result = file1.length().compareTo(file2.length())
        return if (ascending) result else -result
    }

    private fun compareByName(file1: File, file2: File, ascending: Boolean): Int {
        val result = file1.name.compareTo(file2.name, ignoreCase = true)
        return if (ascending) result else -result
    }
}
