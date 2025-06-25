package cc.kafuu.archandler.libs.archive

import java.io.File

interface IPasswordProvider {
    suspend fun getPassword(file: File): String?
}