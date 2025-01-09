package cc.kafuu.archandler.libs.model

data class LoadingState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val displayMark: Boolean = false
)
