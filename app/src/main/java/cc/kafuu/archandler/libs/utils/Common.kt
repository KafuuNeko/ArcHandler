package cc.kafuu.archandler.libs.utils

inline fun <reified T> castOrNull(obj: Any?) = if (obj is T) obj else null
