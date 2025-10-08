package cc.kafuu.archandler.libs.jni

interface NativeCallback {
    fun invoke(vararg args: Any?)
}
