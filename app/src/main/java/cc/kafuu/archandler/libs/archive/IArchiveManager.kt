package cc.kafuu.archandler.libs.archive

import java.io.File
import java.nio.file.Files

interface IArchiveManager {
    fun openArchive(file: File): Result<IArchive>

    fun packageFiles(
        archiveFile: File,
        sourceFiles: List<Files>,
        onProgress: (Int) -> Unit
    ): Boolean
}