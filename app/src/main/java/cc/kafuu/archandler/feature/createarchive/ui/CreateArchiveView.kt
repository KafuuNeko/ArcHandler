package cc.kafuu.archandler.feature.createarchive.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveLoadState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import java.io.File

@Composable
fun CreateArchiveView(
    uiState: CreateArchiveUiState,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit = {}
) {
    when (uiState) {
        CreateArchiveUiState.None, CreateArchiveUiState.Finished -> Unit
        is CreateArchiveUiState.Normal -> {
            NormalView(uiState, emitIntent)
            LoadDialogSwitch(uiState.loadState, emitIntent)
        }
    }
}

@Composable
private fun LoadDialogSwitch(
    loadState: CreateArchiveLoadState,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit
) {
    when (loadState) {
        CreateArchiveLoadState.None -> Unit
        is CreateArchiveLoadState.Packing -> {
            val message = stringResource(loadState.message)
            val filename = loadState.currentFile?.name
            val progress = loadState.progression?.let { "${it.first}/${it.second}" }
            AppLoadDialog(
                messages = listOf(message, filename, progress).mapNotNull { it },
                buttonText = stringResource(R.string.cancel),
                onClickButton = {
                    emitIntent(CreateArchiveUiIntent.CancelPackingJob)
                }
            )
        }
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