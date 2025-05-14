package cc.kafuu.archandler.libs.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ViewEventWrapper<out T>(private val content: T) {
    private val mMutex = Mutex()
    private val mHasHandled = MutableStateFlow(false)

    suspend fun consumeIfNotHandled(handle: suspend (T) -> Unit): Boolean = mMutex.withLock {
        if (mHasHandled.value) return@withLock false
        handle(content)
        mHasHandled.value = true
        return@withLock true
    }


    suspend fun waitForConsumption() {
        mHasHandled.filter { it }.collect()
    }
}

interface IViewEventOwner<out T> {
    val viewEvent: ViewEventWrapper<T>?
}

inline fun <reified E> E.toViewEvent(): ViewEventWrapper<E> = ViewEventWrapper(this)