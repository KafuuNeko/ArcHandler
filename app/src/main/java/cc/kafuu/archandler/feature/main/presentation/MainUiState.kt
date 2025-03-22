package cc.kafuu.archandler.feature.main.presentation

sealed class MainUiState(
    open val loadingState: LoadingState = LoadingState()
) {
    data object None : MainUiState()

    data object PermissionDenied : MainUiState()

    data class Accessible(
        override val loadingState: LoadingState = LoadingState(),
        val errorMessage: String? = null,
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState(loadingState = loadingState)

    data object Finished : MainUiState()
}