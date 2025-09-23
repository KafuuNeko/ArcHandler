package cc.kafuu.archandler.libs.archive

import java.io.File

interface IPacker {
    suspend fun pack(
        files: List<File>,
        listener: (current: Int, total: Int, filePath: String) -> Unit
    ): Boolean
}