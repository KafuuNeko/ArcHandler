package cc.kafuu.archandler.libs.core

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class CoreViewModelWithEvent<I, S, E>(initStatus: S) : CoreViewModel<I, S>(initStatus) {
    // Single Event (Model -> View)
    private val mSingleEventChannel = Channel<E>(Channel.BUFFERED)
    val singleEventFlow = mSingleEventChannel.receiveAsFlow()

    protected fun dispatchingEvent(event: E) {
        viewModelScope.launch {
            mSingleEventChannel.send(event)
        }
    }
}