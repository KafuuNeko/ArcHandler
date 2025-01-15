package cc.kafuu.archandler.feature.main.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.main.presentation.MainListData
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState

@Composable
fun AccessibleView(
    modifier: Modifier = Modifier,
    uiState: MainUiState.Accessible,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    when (val listData = uiState.listData) {
        MainListData.Undecided -> Unit

        is MainListData.StorageVolume -> StorageVolumeView(
            modifier = modifier,
            loadingState = uiState.loadingState,
            listData = listData,
            emitIntent = emitIntent
        )

        is MainListData.Directory -> DirectoryView(
            modifier = modifier,
            loadingState = uiState.loadingState,
            listData = listData,
            viewMode = uiState.viewMode,
            emitIntent = emitIntent
        )
    }
}






