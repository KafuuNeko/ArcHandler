package cc.kafuu.archandler.libs.ext

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

fun Path.getParentPath(): Path? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.parent
    } else {
        File(this.toString()).parent?.let { Path(it) }
    }
}

fun Path.isSameFileOrDirectory(other: Path): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Files.isSameFile(this, other)
    } else {
        val file1 = File(this.toString())
        val file2 = File(other.toString())
        file1.exists() && file2.exists() && file1.absolutePath == file2.absolutePath
    }
}