package cc.kafuu.archandler.feature.archiveview

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiIntent
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewViewEvent
import cc.kafuu.archandler.feature.archiveview.ui.ArchiveViewLayout
import cc.kafuu.archandler.feature.storagepicker.StoragePickerActivity
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent
import cc.kafuu.archandler.libs.core.IViewEvent
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.model.ArchiveViewData
import java.io.File

class ArchiveViewActivity : CoreActivityWithEvent() {
    companion object {
        fun params(
            dtm: DataTransferManager,
            archiveFile: File
        ) = Bundle().apply {
            val data = ArchiveViewData(archiveFile = archiveFile)
            putString(AppModel.KEY_DATA, dtm.push(data))
        }
    }

    private val mViewModel by viewModels<ArchiveViewViewModel>()

    private val mStoragePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        result.data?.getStringExtra(AppModel.KEY_DATA)?.run {
            ArchiveViewUiIntent.ExtractToDirectoryCompleted(this).run { mViewModel.emit(this) }
        }
    }

    override fun getViewEventFlow() = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is ArchiveViewUiState.Finished) finish()
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            ArchiveViewLayout(
                uiState = uiState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArchiveViewUiIntent.Init(
            transferId = intent.getStringExtra(AppModel.KEY_DATA)
        ).run {
            mViewModel.emit(this)
        }
    }

    override suspend fun onReceivedViewEvent(viewEvent: IViewEvent) {
        super.onReceivedViewEvent(viewEvent)
        if (viewEvent is ArchiveViewViewEvent) when (viewEvent) {
            is ArchiveViewViewEvent.SelectExtractDirectory -> onSelectExtractDirectory(viewEvent)
        }
    }

    private fun onSelectExtractDirectory(event: ArchiveViewViewEvent.SelectExtractDirectory) {
        val intent = Intent(this, StoragePickerActivity::class.java).apply {
            putExtras(event.params)
        }
        mStoragePickerLauncher.launch(intent)
    }
}

