package cc.kafuu.archandler.feature.archiveoptions

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cc.kafuu.archandler.feature.archiveoptions.ui.ArchiveOptionsView
import cc.kafuu.archandler.libs.core.CoreActivity

class ArchiveOptionsActivity : CoreActivity() {
    private val mViewModel by viewModels<ArchiveOptionsViewModel>()

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        ArchiveOptionsView(
            uiState = uiState,
            emitIntent = { intent -> mViewModel.emit(intent) }
        )
    }
}