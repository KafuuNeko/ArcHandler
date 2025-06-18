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
            loadState = uiState.loadState,
            listState = listState,
            viewMode = uiState.viewModeState,
            emitIntent = emitIntent
        )

        is MainListState.Directory -> DirectoryView(
            modifier = modifier,
            loadState = uiState.loadState,
            listState = listState,
            viewMode = uiState.viewModeState,
            emitIntent = emitIntent
        )
    }
}






