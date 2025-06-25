package cc.kafuu.archandler.libs.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ResultFuture<T> {
    private val mMutex = Mutex()
    private var mResult: Result<T>? = null
    private var mDeferred: CompletableDeferred<Result<T>>? = null

    /**
     * 设置结果，如果已设置则不会覆盖
     */
    suspend fun setResult(value: Result<T>) = mMutex.withLock {
        if (mResult != null) return@withLock
        mResult = value
        mDeferred?.complete(value)
    }

    /**
     * 获取结果，已有结果则立即返回，否则挂起等待
     */
    suspend fun awaitResult(): Result<T> {
        val resultOrDeferred = mMutex.withLock {
            mResult?.let { return it }
            if (mDeferred == null) {
                mDeferred = CompletableDeferred()
            }
            mDeferred
        }
        return resultOrDeferred!!.await()
    }

    /**
     * 尝试获取结果，若尚未完成返回 null
     */
    suspend fun tryGetResult(): Result<T>? = mMutex.withLock { mResult }

    /**
     * 判断是否已完成
     */
    suspend fun isCompleted(): Boolean = mMutex.withLock { mResult != null }
}
