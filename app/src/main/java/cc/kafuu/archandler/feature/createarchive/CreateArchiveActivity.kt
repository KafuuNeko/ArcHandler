package cc.kafuu.archandler.feature.createarchive

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.feature.createarchive.ui.CreateArchiveLayout
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent

class CreateArchiveActivity : CoreActivityWithEvent() {
    companion object {
        fun params(transferId: String) = Bundle().apply {
            putString(AppModel.KEY_DATA, transferId)
        }
    }

    private val mViewModel by viewModels<CreateArchiveViewModel>()

    override fun getViewEventFlow() = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        CreateArchiveLayout(
            uiState = uiState,
            emitIntent = { intent -> mViewModel.emit(intent) }
        )
        LaunchedEffect(uiState) {
            if (uiState is CreateArchiveUiState.Finished) finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CreateArchiveUiIntent.Init(
            transferId = intent.getStringExtra(AppModel.KEY_DATA)
        ).run {
            mViewModel.emit(this)
        }
    }
}