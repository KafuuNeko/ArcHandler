package cc.kafuu.archandler.libs.extensions

import cc.kafuu.archandler.libs.archive.model.CompressionOption

fun CompressionOption.getNameExtension() = when (this) {
    is CompressionOption.BZip2 -> "bz2"
    is CompressionOption.GZip -> "gz"
    is CompressionOption.SevenZip -> "7z"
    is CompressionOption.Tar -> "tar"
    is CompressionOption.Zip -> "zip"
}