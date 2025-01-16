package cc.kafuu.archandler.feature.main.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState

@Composable
fun AccessibleView(
    modifier: Modifier = Modifier,
    uiState: MainUiState.Accessible,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    when (val listState = uiState.listState) {
        MainListState.Undecided -> Unit

        is MainListState.StorageVolume -> StorageVolumeView(
            modifier = modifier,
            loadingState = uiState.loadingState,
            listState = listState,
            emitIntent = emitIntent
        )

        is MainListState.Directory -> DirectoryView(
            modifier = modifier,
            loadingState = uiState.loadingState,
            listState = listState,
            viewMode = uiState.viewModeState,
            emitIntent = emitIntent
        )
    }
}






