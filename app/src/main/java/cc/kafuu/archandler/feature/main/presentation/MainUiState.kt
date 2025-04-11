package cc.kafuu.archandler.feature.main.presentation

sealed class MainUiState(
    open val loadState: LoadState = LoadState.None
) {
    data object None : MainUiState()

    data object PermissionDenied : MainUiState()

    data class Accessible(
        override val loadState: LoadState = LoadState.None,
        val errorMessage: String? = null,
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState(loadState = loadState)

    data object Finished : MainUiState()
}