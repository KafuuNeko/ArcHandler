package cc.kafuu.archandler.vm

import cc.kafuu.archandler.libs.core.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : CoreViewModel<MainUiIntent>() {
    private val mUiStateFlow = MutableStateFlow<MainUiState>(
        MainUiState.Init
    )
    val uiState = mUiStateFlow.asStateFlow()

    override fun onCollectedIntent(uiIntent: MainUiIntent) {
        TODO("Not yet implemented")
    }
}

sealed class MainUiIntent

sealed class MainUiState {
    data object Init : MainUiState()
}