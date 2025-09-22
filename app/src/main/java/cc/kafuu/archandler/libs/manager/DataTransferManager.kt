package cc.kafuu.archandler.libs.manager

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DataTransferManager {
    private data class Entry(val value: Any)

    // 存储
    private val mStore = ConcurrentHashMap<String, Entry>()

    /**
     * 推入对象
     * @return token（UUID 字符串）
     */
    fun push(obj: Any): String {
        val token = UUID.randomUUID().toString()
        mStore[token] = Entry(obj)
        return token
    }

    /**
     * 取出并删除。
     */
    fun take(token: String?): Any? {
        if (token.isNullOrEmpty()) return null
        val entry = mStore.remove(token) ?: return null
        return entry.value
    }

    inline fun <reified T> takeAs(token: String?): T? {
        return take(token) as? T
    }
}
