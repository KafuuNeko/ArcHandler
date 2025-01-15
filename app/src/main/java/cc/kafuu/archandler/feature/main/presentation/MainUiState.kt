package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.model.LoadingState

sealed class MainUiState(
    open val loadingState: LoadingState = LoadingState()
) {
    data object PermissionDenied : MainUiState()

    data class Accessible(
        override val loadingState: LoadingState = LoadingState(),
        val errorMessage: String? = null,
        val viewMode: MainListViewMode = MainListViewMode.Normal,
        val listData: MainListData = MainListData.Undecided,
    ) : MainUiState(loadingState = loadingState)
}