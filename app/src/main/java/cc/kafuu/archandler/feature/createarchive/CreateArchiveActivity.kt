package cc.kafuu.archandler.feature.createarchive

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
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveViewEvent
import cc.kafuu.archandler.feature.createarchive.ui.CreateArchiveLayout
import cc.kafuu.archandler.feature.storagepicker.StoragePickerActivity
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent
import cc.kafuu.archandler.libs.core.IViewEvent
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.model.CreateArchiveData
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

class CreateArchiveActivity : CoreActivityWithEvent() {
    companion object {
        fun params(
            dtm: DataTransferManager,
            files: List<File>,
            targetStorageData: StorageData,
            targetDirectoryPath: Path
        ) = Bundle().apply {
            val data = CreateArchiveData(
                files = files,
                targetStorageData = targetStorageData,
                targetDirectoryPath = targetDirectoryPath
            )
            putString(AppModel.KEY_DATA, dtm.push(data))
        }
    }

    private val mViewModel by viewModels<CreateArchiveViewModel>()

    private val mStoragePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        result.data?.getStringExtra(AppModel.KEY_DATA)?.run {
            CreateArchiveUiIntent.SelectFolderCompleted(this).run { mViewModel.emit(this) }
        }
    }

    override fun getViewEventFlow() = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is CreateArchiveUiState.Finished) finish()
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            CreateArchiveLayout(
                uiState = uiState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
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

    override suspend fun onReceivedViewEvent(viewEvent: IViewEvent) {
        super.onReceivedViewEvent(viewEvent)
        if (viewEvent is CreateArchiveViewEvent) when (viewEvent) {
            is CreateArchiveViewEvent.SelectFolder -> onSelectFolder(viewEvent)
        }
    }

    private fun onSelectFolder(event: CreateArchiveViewEvent.SelectFolder) {
        val intent = Intent(this, StoragePickerActivity::class.java).apply {
            putExtras(event.params)
        }
        mStoragePickerLauncher.launch(intent)
    }
}