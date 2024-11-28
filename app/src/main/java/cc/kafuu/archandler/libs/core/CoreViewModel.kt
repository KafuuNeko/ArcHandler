package cc.kafuu.archandler.libs.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class CoreViewModel<I> : ViewModel() {
    private val mUiIntentFlow = MutableSharedFlow<I>()

    init {
        viewModelScope.launch {
            mUiIntentFlow.collect {
                onCollectedIntent(it)
            }
        }
    }

    fun emit(uiIntent: I) {
        viewModelScope.launch {
            mUiIntentFlow.emit(uiIntent)
        }
    }

    protected abstract fun onCollectedIntent(uiIntent: I)

    protected fun <T> Flow<T>.stateInThis(): StateFlow<T?> {
        return stateIn(viewModelScope, SharingStarted.Lazily, null)
    }
}