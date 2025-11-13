package cc.kafuu.archandler.libs.jni.model

enum class LibArchiveFormat(val id: Int) {
    TarUstar(0),
    TarPax(1),
    TarGnu(2),
    TarV7(3),
    Cpio(4),
    Zip(5),
    Xar(6)
}