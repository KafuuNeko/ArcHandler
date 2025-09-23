package cc.kafuu.archandler.feature.createarchive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveViewEvent
import cc.kafuu.archandler.feature.createarchive.ui.CreateArchiveView
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreActivity

class CreateArchiveActivity : CoreActivity() {
    companion object {
        fun startActivity(context: Context, transferId: String) {
            val intent = Intent(context, CreateArchiveActivity::class.java).apply {
                putExtra(AppModel.KEY_DATA, transferId)
            }
            context.startActivity(intent)
        }
    }

    private val mViewModel by viewModels<CreateArchiveViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CreateArchiveUiIntent.Init(
            transferId = intent.getStringExtra(AppModel.KEY_DATA)
        ).run {
            mViewModel.emit(this)
        }
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        CreateArchiveView(
            uiState = uiState,
            emitIntent = { intent -> mViewModel.emit(intent) }
        )
        LaunchedEffect(Unit) {
            mViewModel.collectEvent { onViewEvent(it) }
        }
        LaunchedEffect(uiState) {
            if (uiState is CreateArchiveUiState.Finished) finish()
        }
    }

    private fun onViewEvent(event: CreateArchiveViewEvent) {
        when (event) {
            is CreateArchiveViewEvent.ToastMessageByResId -> onToastMessageByResId(event)
        }
    }

    private fun onToastMessageByResId(event: CreateArchiveViewEvent.ToastMessageByResId) {
        Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
    }
}