package cc.kafuu.archandler.feature.settings

import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiIntent
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiState
import cc.kafuu.archandler.feature.settings.ui.SettingsLayout
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent

class SettingsActivity : CoreActivityWithEvent() {

    private val mViewModel by viewModels<SettingsViewModel>()

    override fun getViewEventFlow() = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()

        LaunchedEffect(uiState) {
            if (uiState is SettingsUiState.Finished) {
                finish()
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsLayout(
                uiState = uiState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.emit(SettingsUiIntent.Init)
    }
}
