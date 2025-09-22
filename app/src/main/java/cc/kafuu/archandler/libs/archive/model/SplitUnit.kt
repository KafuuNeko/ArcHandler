package cc.kafuu.archandler.libs.archive.model

enum class SplitUnit(val display: String, val multiplier: Long) {
    MB("MB", 1024L * 1024L),
    GB("GB", 1024L * 1024L * 1024L);
}
