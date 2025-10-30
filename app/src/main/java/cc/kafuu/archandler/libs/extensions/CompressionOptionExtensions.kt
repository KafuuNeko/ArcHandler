package cc.kafuu.archandler.libs.extensions

import cc.kafuu.archandler.libs.archive.model.CompressionOption

fun CompressionOption.getNameExtension() = when (this) {
    is CompressionOption.BZip2 -> "bz2"
    is CompressionOption.GZip -> "gz"
    is CompressionOption.SevenZip -> "7z"
    is CompressionOption.Tar -> "tar"
    is CompressionOption.Zip -> "zip"
    is CompressionOption.TarBzip2 -> "tar.bz2"
    is CompressionOption.TarGZip -> "tar.gz"
    is CompressionOption.TarXz -> "tar.xz"
    is CompressionOption.TarLz4 -> "tar.lz4"
    is CompressionOption.TarZstd -> "tar.zst"
    is CompressionOption.Cpio -> "cpio"
    is CompressionOption.CpioBzip2 -> "cpio.bz2"
    is CompressionOption.CpioGZip -> "cpio.gz"
    is CompressionOption.CpioXz -> "cpio.xz"
    is CompressionOption.CpioLz4 -> "cpio.lz4"
    is CompressionOption.CpioZstd -> "cpio.zst"
}