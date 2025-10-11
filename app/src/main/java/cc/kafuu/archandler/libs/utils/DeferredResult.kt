package cc.kafuu.archandler.libs.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DeferredResult<T> {
    private data class ValueWrapper<T>(val value: T)

    private val mMutex = Mutex()
    private var mResult: ValueWrapper<T>? = null
    private var mDeferred: CompletableDeferred<ValueWrapper<T>>? = null

    /**
     * 设置结果，如果已设置则不会覆盖
     */
    suspend fun complete(value: T) = mMutex.withLock {
        if (mResult != null) return@withLock
        ValueWrapper(value).run {
            mResult = this
            mDeferred?.complete(this)
        }
    }

    fun complete(coroutineScope: CoroutineScope, value: T) {
        coroutineScope.launch { complete(value) }
    }

    /**
     * 获取结果，已有结果则立即返回，否则挂起等待
     */
    suspend fun awaitCompleted(): T {
        val resultOrDeferred = mMutex.withLock {
            mResult?.let { return it.value }
            if (mDeferred == null) {
                mDeferred = CompletableDeferred()
            }
            mDeferred
        }
        return resultOrDeferred!!.await().value
    }

    /**
     * 判断是否已完成
     */
    suspend fun isCompleted(): Boolean = mMutex.withLock { mResult != null }
}
