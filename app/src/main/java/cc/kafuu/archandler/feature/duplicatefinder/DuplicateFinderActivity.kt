package cc.kafuu.archandler.feature.duplicatefinder

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderUiIntent
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderUiState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderViewEvent
import cc.kafuu.archandler.feature.duplicatefinder.ui.DuplicateFinderLayout
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent
import cc.kafuu.archandler.libs.core.IViewEvent
import java.io.File

class DuplicateFinderActivity : CoreActivityWithEvent() {

    companion object {
        const val EXTRA_DIRECTORY_PATH = "directory_path"

        fun params(directory: File): Bundle {
            return Bundle().apply {
                putString(EXTRA_DIRECTORY_PATH, directory.absolutePath)
            }
        }
    }

    private val directory: File by lazy {
        val directoryPath = intent.getStringExtra(EXTRA_DIRECTORY_PATH)
        File(directoryPath ?: throw IllegalArgumentException("Directory path is required"))
    }

    private val mViewModel: DuplicateFinderViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DuplicateFinderViewModel(directory) as T
            }
        }
    }

    override fun getViewEventFlow() = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()

        BackHandler {
            mViewModel.emit(DuplicateFinderUiIntent.Back)
        }

        LaunchedEffect(uiState) {
            if (uiState is DuplicateFinderUiState.Finished) finish()
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            DuplicateFinderLayout(
                uiState = uiState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(DuplicateFinderUiIntent.Init)
    }

    override suspend fun onReceivedViewEvent(viewEvent: IViewEvent) {
        super.onReceivedViewEvent(viewEvent)
        when (viewEvent) {
            is DuplicateFinderViewEvent.Finish -> finish()
            else -> Unit
        }
    }
}
