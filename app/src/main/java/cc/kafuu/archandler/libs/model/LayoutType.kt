package cc.kafuu.archandler.libs.model

/**
 * 列表布局类型枚举
 * 用于在不同页面间统一列表显示样式
 */
enum class LayoutType(val value: Int) {
    /**
     * 列表布局 - 垂直列表形式，一行一个项目
     */
    LIST(0),

    /**
     * 网格布局 - 网格形式，一行四个项目，图标在上，文件名在下
     */
    GRID(1);

    companion object {
        /**
         * 根据整数值获取对应的布局类型
         * @param value 布局类型的整数值
         * @return 对应的布局类型，如果无效值则返回 LIST
         */
        fun fromValue(value: Int): LayoutType {
            return entries.firstOrNull { it.value == value } ?: LIST
        }
    }

    /**
     * 切换到另一种布局类型
     * @return 如果当前是 LIST 返回 GRID，反之亦然
     */
    fun toggle(): LayoutType {
        return if (this == LIST) GRID else LIST
    }
}
