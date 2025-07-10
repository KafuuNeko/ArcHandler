package cc.kafuu.archandler.feature.archiveoptions.presentation

import java.io.File

sealed class ArchiveOptionsUiState {
    data object None : ArchiveOptionsUiState()

    data class Normal(
        val files: List<File>,
        val targetDirectory: File,
    ) : ArchiveOptionsUiState()
}