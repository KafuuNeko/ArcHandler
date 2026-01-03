package cc.kafuu.archandler.feature.archiveview.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewLoadState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiIntent
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiState
import cc.kafuu.archandler.libs.extensions.getReadableSize
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppLazyColumn
import cc.kafuu.archandler.ui.widges.AppTopBar
import cc.kafuu.archandler.ui.widges.IconMessageView
import java.io.File

@Composable
fun ArchiveViewLayout(
    uiState: ArchiveViewUiState,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    when (uiState) {
        ArchiveViewUiState.None, ArchiveViewUiState.Finished -> Unit

        is ArchiveViewUiState.Normal -> {
            BackHandler { emitIntent(ArchiveViewUiIntent.Back) }
            NormalView(uiState, emitIntent)
            LoadDialogSwitch(uiState.loadState, emitIntent)
        }
    }
}

@Composable
private fun LoadDialogSwitch(
    loadState: ArchiveViewLoadState,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit
) {
    when (loadState) {
        ArchiveViewLoadState.None -> Unit

        ArchiveViewLoadState.ArchiveOpening -> {
            AppLoadDialog(message = stringResource(R.string.archive_opening_message))
        }

        ArchiveViewLoadState.LoadingEntries -> {
            AppLoadDialog(message = stringResource(R.string.loading_entries_message))
        }

        is ArchiveViewLoadState.Extracting -> {
            val message = stringResource(R.string.unpacking_file_message)
            val progress = if (loadState.total > 0) {
                "${loadState.index}/${loadState.total}"
            } else null
            val currentFile = loadState.path
            AppLoadDialog(
                messages = listOf(message, progress, currentFile).mapNotNull { it },
                buttonText = stringResource(R.string.cancel),
                onClickButton = {
                    emitIntent(ArchiveViewUiIntent.CancelExtracting)
                }
            )
        }
    }
}

@Composable
private fun NormalView(
    uiState: ArchiveViewUiState.Normal,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                title = uiState.archiveFile.name,
                onBack = { emitIntent(ArchiveViewUiIntent.Back) }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 显示当前路径
            if (uiState.currentPath.isNotEmpty()) {
                PathBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    currentPath = uiState.currentPath,
                    archiveFileName = uiState.archiveFile.name
                )
            }

            // 条目列表
            AppLazyColumn(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                state = uiState.lazyListState,
                items = uiState.entries,
                emptyState = {
                    if (uiState.loadState !is ArchiveViewLoadState.None) {
                        Spacer(modifier = Modifier.weight(1f))
                        return@AppLazyColumn
                    }
                    IconMessageView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        icon = painterResource(R.drawable.ic_empty_folder),
                        message = stringResource(R.string.empty_directory),
                    )
                }
            ) { entry ->
                EntryItem(
                    entry = entry,
                    emitIntent = emitIntent
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 底部解压按钮
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            ExtractButton(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 10.dp),
                emitIntent = emitIntent
            )
        }
    }
}

@Composable
private fun PathBar(
    modifier: Modifier = Modifier,
    currentPath: String,
    archiveFileName: String
) {
    // 简单的路径显示，可以后续优化为可点击的路径导航
    androidx.compose.material3.Text(
        modifier = modifier,
        text = "$archiveFileName / $currentPath",
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun EntryItem(
    entry: cc.kafuu.archandler.libs.archive.model.ArchiveEntry,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    val icon = if (entry.isDirectory) {
        painterResource(R.drawable.ic_folder)
    } else {
        painterResource(R.drawable.ic_file)
    }
    val secondaryText = if (!entry.isDirectory) entry.size.getReadableSize() else null

    AppIconTextItemCard(
        modifier = Modifier.fillMaxWidth(),
        painter = icon,
        text = entry.name,
        secondaryText = secondaryText
    ) {
        emitIntent(ArchiveViewUiIntent.EntrySelected(entry))
    }
}

@Composable
private fun ExtractButton(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    androidx.compose.material3.Button(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        onClick = { emitIntent(ArchiveViewUiIntent.ExtractArchive) }
    ) {
        androidx.compose.material3.Text(stringResource(R.string.extract_archive))
    }
}

@Preview
@Composable
private fun ArchiveViewPreview() {
    ArchiveViewLayout(ArchiveViewUiState.Normal(archiveFile = File("archive.zip")))
}