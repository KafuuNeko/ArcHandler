package cc.kafuu.archandler.libs.jni.model

enum class LibCompressionType(val id: Int) {
    None(0),
    Gzip(1),
    Bzip2(2),
    Xz(3),
    Lz4(4)
}
