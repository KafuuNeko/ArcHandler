package cc.kafuu.archandler.libs.archive

interface IPasswordProvider {
    suspend fun getPassword(): String?
}