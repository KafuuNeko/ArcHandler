package cc.kafuu.archandler.libs.jni

object NativeLib {
    init {
        System.loadLibrary("archandler")
    }

    external fun getLatestErrorMessage(): String

    external fun createArchive(
        outputPath: String,
        baseDir: String,
        inputFiles: List<String>,
        format: Int,
        compression: Int,
        compressionLevel: Int,
        listener: NativeCallback
    ): Boolean

    external fun extractArchive(
        archivePath: String,
        outputDir: String,
        listener: NativeCallback,
        overwrite: Boolean = true
    ): Boolean
}