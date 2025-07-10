package cc.kafuu.archandler.feature.archiveoptions.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.archiveoptions.presentation.ArchiveOptionsUiIntent
import cc.kafuu.archandler.feature.archiveoptions.presentation.ArchiveOptionsUiState
import cc.kafuu.archandler.ui.widges.AppTopBar
import java.io.File

@Composable
fun ArchiveOptionsView(
    uiState: ArchiveOptionsUiState,
    emitIntent: (uiIntent: ArchiveOptionsUiIntent) -> Unit = {}
) {
    when (uiState) {
        ArchiveOptionsUiState.None -> Unit
        is ArchiveOptionsUiState.Normal -> NormalView(uiState, emitIntent)
    }
}

@Composable
private fun NormalView(
    uiState: ArchiveOptionsUiState.Normal,
    emitIntent: (uiIntent: ArchiveOptionsUiIntent) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.create_archive),
                onBack = { emitIntent(ArchiveOptionsUiIntent.Back) }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

        }
    }
}


@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun ArchiveOptionsViewPreview() {
    ArchiveOptionsView(
        uiState = ArchiveOptionsUiState.Normal(emptyList(), File("")),
        emitIntent = {}
    )
}