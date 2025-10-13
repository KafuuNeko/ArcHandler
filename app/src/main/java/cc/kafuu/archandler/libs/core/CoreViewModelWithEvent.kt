package cc.kafuu.archandler.libs.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 带 UiEvent 的 CoreViewModel
 */
abstract class CoreViewModelWithEvent<I, S>(initStatus: S) : CoreViewModel<I, S>(initStatus) {
    private val mUiEventFlow = MutableSharedFlow<ViewEventWrapper>(extraBufferCapacity = 64)
    val uiEventFlow = mUiEventFlow.asSharedFlow()

    /**
     * 捕获View事件
     */
    suspend fun collectEvent(
        lifecycleOwner: LifecycleOwner,
        handle: suspend (IViewEvent) -> Unit
    ) {
        uiEventFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.CREATED)
            .collect { event -> event.consumeIfNotHandled(handle) }
    }

    /**
     * 尝试分发UI Event（一次性事件）
     */
    protected fun IViewEvent.tryEmit(): Boolean {
        return mUiEventFlow.tryEmit(ViewEventWrapper(this))
    }

    /**
     * 分发UI Event，缓冲区满则等待
     */
    protected suspend fun IViewEvent.emit() {
        mUiEventFlow.emit(ViewEventWrapper(this))
    }

    /**
     * 发一个 UI Event, 并等待其事件消费完成
     */
    protected suspend fun IViewEvent.emitAndAwait() {
        ViewEventWrapper(this)
            .apply { mUiEventFlow.emit(this) }
            .waitForConsumption()
    }
}

class ViewEventWrapper(private val content: IViewEvent) {
    private val mMutex = Mutex()
    private val mHasHandled = MutableStateFlow(false)

    suspend fun consumeIfNotHandled(handle: suspend (IViewEvent) -> Unit) = mMutex.withLock {
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

interface IViewEvent

