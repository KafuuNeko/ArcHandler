package cc.kafuu.archandler.libs.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class CoreViewModel<I, S>(initStatus: S) : ViewModel() {
    // Ui State (Model -> View)
    private val mUiStateFlow = MutableStateFlow<S>(initStatus)
    val uiStateFlow = mUiStateFlow.asStateFlow()

    // Ui Intent (View -> Model)
    private val mUiIntentFlow = MutableSharedFlow<I>()

    init {
        viewModelScope.launch {
            mUiIntentFlow.collect {
                onReceivedUiIntent(it)
            }
        }
    }

    fun emit(uiIntent: I) {
        viewModelScope.launch {
            mUiIntentFlow.emit(uiIntent)
        }
    }

    protected abstract fun onReceivedUiIntent(uiIntent: I)

    protected fun <T> Flow<T>.stateInThis(): StateFlow<T?> {
        return stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    protected fun fetchUiState() = mUiStateFlow.value

    protected suspend inline fun <reified T> awaitUiStateOfType(): T {
        return uiStateFlow.filterIsInstance<T>().first()
    }

    protected fun S.setup() {
        mUiStateFlow.value = this
    }
}