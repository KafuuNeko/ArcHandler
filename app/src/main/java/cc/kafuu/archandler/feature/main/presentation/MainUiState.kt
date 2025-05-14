package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.core.IViewEventOwner
import cc.kafuu.archandler.libs.core.ViewEventWrapper

sealed class MainUiState {
    data object None : MainUiState()

    data class PermissionDenied(
        override val viewEvent: ViewEventWrapper<MainViewEvent>? = null
    ) : MainUiState(), IViewEventOwner<MainViewEvent>

    data class Accessible(
        override val viewEvent: ViewEventWrapper<MainViewEvent>? = null,
        val loadState: MainLoadState = MainLoadState.None,
        val errorMessage: String? = null,
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState(), IViewEventOwner<MainViewEvent>

    data object Finished : MainUiState()
}