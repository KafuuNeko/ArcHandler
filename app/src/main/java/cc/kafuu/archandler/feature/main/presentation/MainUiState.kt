package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.core.ViewEventWrapper

sealed class MainUiState {
    data object None : MainUiState()

    data object PermissionDenied : MainUiState()

    data class Accessible(
        val viewEvent: ViewEventWrapper<MainViewEvent>? = null,
        val loadState: MainLoadState = MainLoadState.None,
        val errorMessage: String? = null,
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState()

    data object Finished : MainUiState()
}