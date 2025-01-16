package cc.kafuu.archandler.feature.main.presentation

data class LoadingState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val displayMark: Boolean = false
)
