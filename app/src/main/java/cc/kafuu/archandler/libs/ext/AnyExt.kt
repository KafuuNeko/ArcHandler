package cc.kafuu.archandler.libs.ext

inline fun <reified T> Any.castOrNull(): T? = if (this is T) this else null