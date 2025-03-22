package cc.kafuu.archandler.libs.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class CoreViewModelWithEvent<I, S, E>(initStatus: S) : CoreViewModel<I, S>(initStatus) {
    // Single Event (Model -> View)
    private val mSingleEventFlow = MutableSharedFlow<E>()
    val singleEventFlow = mSingleEventFlow.asSharedFlow()

    protected fun dispatchingEvent(event: E) {
        viewModelScope.launch {
            mSingleEventFlow.emit(event)
        }
    }
}