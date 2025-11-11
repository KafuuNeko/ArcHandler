package cc.kafuu.archandler.feature.importfile.presention

import cc.kafuu.archandler.libs.utils.DeferredResult


sealed class ImportFileUiState {
    data object None : ImportFileUiState()

    data class Normal(
        val dialogState: ImportFileDialogState = ImportFileDialogState.None
    ) : ImportFileUiState()

    data object Finished : ImportFileUiState()
}

sealed class ImportFileDialogState {
    data object None : ImportFileDialogState()

    data class ImportConfirm(
        val count: Int,
        val deferredResult: DeferredResult<Boolean> = DeferredResult()
    ) : ImportFileDialogState()

    data class Importing(val name: String? = null) : ImportFileDialogState()
}