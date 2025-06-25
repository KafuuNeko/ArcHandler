package cc.kafuu.archandler.feature.main.presentation

import cc.kafuu.archandler.libs.core.IViewEventOwner
import cc.kafuu.archandler.libs.core.ViewEventWrapper
import cc.kafuu.archandler.libs.utils.ResultFuture
import java.io.File

sealed class MainUiState {
    data object None : MainUiState()

    data class PermissionDenied(
        override val viewEvent: ViewEventWrapper<MainViewEvent>? = null
    ) : MainUiState(), IViewEventOwner<MainViewEvent>

    data class Accessible(
        override val viewEvent: ViewEventWrapper<MainViewEvent>? = null,
        val loadState: MainLoadState = MainLoadState.None,
        val dialogStates: Set<MainDialogState> = emptySet(),
        val errorMessage: String? = null,
        val viewModeState: MainListViewModeState = MainListViewModeState.Normal,
        val listState: MainListState = MainListState.Undecided,
    ) : MainUiState(), IViewEventOwner<MainViewEvent>

    data object Finished : MainUiState()
}

sealed class MainDialogState() {
    data class PasswordInput(
        val file: File,
        val resultFuture: ResultFuture<String> = ResultFuture()
    ) : MainDialogState()

    data class FileDeleteConfirm(
        val fileSet: Set<File>,
        val resultFuture: ResultFuture<Boolean> = ResultFuture()
    ) : MainDialogState()
}