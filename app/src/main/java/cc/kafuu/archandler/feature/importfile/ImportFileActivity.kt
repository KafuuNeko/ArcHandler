package cc.kafuu.archandler.feature.importfile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.importfile.presention.ImportFileUiIntent
import cc.kafuu.archandler.feature.importfile.presention.ImportFileUiState
import cc.kafuu.archandler.feature.importfile.ui.ImportFileLayout
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent
import cc.kafuu.archandler.libs.core.ViewEventWrapper
import cc.kafuu.archandler.libs.extensions.canOpenUri
import cc.kafuu.archandler.libs.extensions.getFileNameWithExtension
import kotlinx.coroutines.flow.Flow

class ImportFileActivity : CoreActivityWithEvent() {
    private val mViewModel by viewModels<ImportFileViewModel>()

    override fun getViewEventFlow(): Flow<ViewEventWrapper> = mViewModel.viewEventFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is ImportFileUiState.Finished) finish()
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            ImportFileLayout(uiState = uiState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val uris = getUris()
            ?.filter { canOpenUri(it) }
            ?.map { getFileNameWithExtension(it, "imported_") to it }
        if (uris == null || uris.isEmpty()) {
            Toast.makeText(this, R.string.no_importable_files_found, Toast.LENGTH_SHORT).show()
            return
        }
        super.onCreate(savedInstanceState)
        mViewModel.emit(ImportFileUiIntent.Init(uris = uris))
    }

    private fun getUris() = when (intent?.action) {
        Intent.ACTION_VIEW -> intent.data?.let { listOf(it) }

        Intent.ACTION_SEND -> intent.getParcelableExtra(
            Intent.EXTRA_STREAM, Uri::class.java
        )?.let { listOf(it) }

        Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra(
            Intent.EXTRA_STREAM, Uri::class.java
        )?.toList()

        else -> null
    }
}