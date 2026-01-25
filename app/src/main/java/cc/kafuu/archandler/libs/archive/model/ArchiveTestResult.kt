package cc.kafuu.archandler.libs.archive.model

/**
 * 压缩包测试结果
 */
data class ArchiveTestResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val testedFiles: Int = 0,
    val totalFiles: Int = 0
) {
    companion object {
        fun success(testedFiles: Int, totalFiles: Int) =
            ArchiveTestResult(success = true, testedFiles = testedFiles, totalFiles = totalFiles)

        fun error(message: String) =
            ArchiveTestResult(success = false, errorMessage = message)
    }
}
