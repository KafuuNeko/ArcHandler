package cc.kafuu.archandler.feature.createarchive.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import java.io.File

@Composable
fun CreateArchiveView(
    uiState: CreateArchiveUiState,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit = {}
) {
    when (uiState) {
        CreateArchiveUiState.None, CreateArchiveUiState.Finished -> Unit
        is CreateArchiveUiState.Normal -> NormalView(uiState, emitIntent)
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun ArchiveOptionsViewPreview() {
    CreateArchiveView(
        uiState = CreateArchiveUiState.Normal(emptyList(), File("")),
        emitIntent = {}
    )
}