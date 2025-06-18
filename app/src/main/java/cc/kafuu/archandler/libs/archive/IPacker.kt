package cc.kafuu.archandler.libs.archive

interface IPacker {
    fun pack(listener: (current: Int, total: Int, filePath: String) -> Unit): Boolean
}