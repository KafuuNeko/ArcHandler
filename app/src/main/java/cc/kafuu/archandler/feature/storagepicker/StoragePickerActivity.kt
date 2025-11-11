package cc.kafuu.archandler.feature.storagepicker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.storagepicker.model.StoragePickerParams
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiIntent
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiState
import cc.kafuu.archandler.feature.storagepicker.ui.StoragePickerLayout
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent
import cc.kafuu.archandler.libs.core.ViewEventWrapper
import cc.kafuu.archandler.libs.manager.DataTransferManager
import kotlinx.coroutines.flow.Flow

class StoragePickerActivity : CoreActivityWithEvent() {
    companion object {
        fun params(dtm: DataTransferManager, data: StoragePickerParams) = Bundle().apply {
            putString(AppModel.KEY_DATA, dtm.push(data))
        }
    }

    private val mViewModel by viewModels<StoragePickerViewModel>()

    override fun getViewEventFlow(): Flow<ViewEventWrapper> = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is StoragePickerUiState.Finished) finish()
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            StoragePickerLayout(
                uiState = uiState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StoragePickerUiIntent.Init(intent.getStringExtra(AppModel.KEY_DATA))
            .run { mViewModel.emit(this) }
    }
}