package net.sf.sevenzipjbinding;

/**
 * Compression methods supported by writable 7z and Zip archives.
 */
public enum OutArchiveCompressionMethod {
    COPY("Copy"),
    DEFLATE("Deflate"),
    LZMA("LZMA"),
    LZMA2("LZMA2"),
    PPMD("PPMD");

    private final String methodName;

    OutArchiveCompressionMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
