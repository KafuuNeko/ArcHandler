package cc.kafuu.archandler.feature.importfile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.importfile.presention.ImportFileDialogState
import cc.kafuu.archandler.feature.importfile.presention.ImportFileUiState
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.dialogs.TextConfirmDialog

@Composable
fun ImportFileLayout(uiState: ImportFileUiState) {
    when (uiState) {
        ImportFileUiState.None,
        ImportFileUiState.Finished -> Unit

        is ImportFileUiState.Normal -> {
            ImportFileDialogSwitch(uiState.dialogState)
        }
    }
}

@Composable
private fun ImportFileDialogSwitch(dialogState: ImportFileDialogState) {
    val coroutineScope = rememberCoroutineScope()
    when (dialogState) {
        ImportFileDialogState.None -> Unit

        is ImportFileDialogState.ImportConfirm -> TextConfirmDialog(
            message = stringResource(R.string.file_import_confirm_message, dialogState.count),
            onDismissRequest = {
                dialogState.deferredResult.complete(coroutineScope, false)
            },
            onConfirmRequest = {
                dialogState.deferredResult.complete(coroutineScope, true)
            }
        )

        is ImportFileDialogState.Importing -> AppLoadDialog(
            messages = listOf(
                stringResource(R.string.importing_message),
                dialogState.name
            ).mapNotNull { it }
        )
    }
}