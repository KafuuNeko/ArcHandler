package cc.kafuu.archandler.libs.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 带 UiEvent 的 CoreViewModel
 */
abstract class CoreViewModelWithEvent<I, S, E>(initStatus: S) : CoreViewModel<I, S>(initStatus) {
    private val mUiEventFlow = MutableSharedFlow<ViewEventWrapper<E>>(extraBufferCapacity = 64)
    val uiEventFlow = mUiEventFlow.asSharedFlow()

    /**
     * 捕获View事件
     */
    suspend fun collectEvent(handle: suspend (E) -> Unit) {
        uiEventFlow.collect { event -> event.consumeIfNotHandled(handle) }
    }

    /**
     * 尝试分发UI Event（一次性事件）
     */
    protected fun E.tryEmit(): Boolean {
        return mUiEventFlow.tryEmit(ViewEventWrapper(this))
    }

    /**
     * 分发UI Event，缓冲区满则等待
     */
    protected suspend fun E.emit() {
        mUiEventFlow.emit(ViewEventWrapper(this))
    }

    /**
     * 发一个 UI Event, 并等待其事件消费完成
     */
    protected suspend fun E.emitAndAwait() {
        ViewEventWrapper(this)
            .apply { mUiEventFlow.emit(this) }
            .waitForConsumption()
    }
}

class ViewEventWrapper<out T>(private val content: T) {
    private val mMutex = Mutex()
    private val mHasHandled = MutableStateFlow(false)

    suspend fun consumeIfNotHandled(handle: suspend (T) -> Unit): Boolean = mMutex.withLock {
        if (mHasHandled.value) return@withLock false
        handle(content)
        mHasHandled.value = true
        return@withLock true
    }

    fun isHandled() = mHasHandled.value

    suspend fun waitForConsumption() {
        if (mHasHandled.value) return
        mHasHandled.first { it }
    }
}