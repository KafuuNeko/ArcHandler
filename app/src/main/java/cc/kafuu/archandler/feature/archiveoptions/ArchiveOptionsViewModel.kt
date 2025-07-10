package cc.kafuu.archandler.feature.archiveoptions

import cc.kafuu.archandler.feature.archiveoptions.presentation.ArchiveOptionsUiIntent
import cc.kafuu.archandler.feature.archiveoptions.presentation.ArchiveOptionsUiState
import cc.kafuu.archandler.libs.core.CoreViewModel

class ArchiveOptionsViewModel : CoreViewModel<ArchiveOptionsUiIntent, ArchiveOptionsUiState>(
    initStatus = ArchiveOptionsUiState.None
) {

}