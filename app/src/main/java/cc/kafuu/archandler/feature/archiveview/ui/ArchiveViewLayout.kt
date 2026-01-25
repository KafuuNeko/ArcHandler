package cc.kafuu.archandler.feature.archiveview.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewDialogState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewLoadState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiIntent
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiState
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.extensions.getReadableSize
import cc.kafuu.archandler.libs.model.LayoutType
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.dialogs.PasswordInputDialog
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppLazyColumn
import cc.kafuu.archandler.ui.widges.AppTopBar
import cc.kafuu.archandler.ui.widges.AppGridFileItemCard
import cc.kafuu.archandler.ui.widges.AppLazyGridView
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
            DialogSwitch(uiState.dialogState)
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
    val isGridLayout = uiState.layoutType == LayoutType.GRID

    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                title = uiState.archiveFile.name,
                onBack = { emitIntent(ArchiveViewUiIntent.Close) },
                backIconPainter = painterResource(R.drawable.ic_close),
                actions = {
                    // 布局切换按钮
                    Image(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(24.dp)
                            .clickable {
                                emitIntent(
                                    ArchiveViewUiIntent.SwitchLayoutType(uiState.layoutType.toggle())
                                )
                            },
                        painter = painterResource(
                            if (uiState.layoutType == LayoutType.LIST) {
                                R.drawable.ic_grid_view
                            } else {
                                R.drawable.ic_list_view
                            }
                        ),
                        contentDescription = stringResource(R.string.switch_layout_type)
                    )
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 显示当前路径
            PathBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                currentPath = uiState.currentPath,
                archiveFileName = uiState.archiveFile.name,
                emitIntent = emitIntent
            )

            if (isGridLayout) {
                // 网格布局
                AppLazyGridView(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                    items = uiState.entries,
                    emptyView = {
                        if (uiState.loadState !is ArchiveViewLoadState.None) {
                            Spacer(modifier = Modifier.weight(1f))
                            return@AppLazyGridView
                        }
                        IconMessageView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            icon = painterResource(R.drawable.ic_empty_folder),
                            message = stringResource(R.string.empty_directory),
                        )
                    },
                    gridItemContent = { entry ->
                        GridEntryItem(
                            entry = entry,
                            emitIntent = emitIntent
                        )
                    }
                )
            } else {
                // 列表布局
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
    archiveFileName: String,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    val pathSegments = if (currentPath.isEmpty()) {
        emptyList()
    } else {
        currentPath.trim('/').split('/').filter { it.isNotEmpty() }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(pathSegments, currentPath) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 压缩包名称（根目录）
        Text(
            modifier = Modifier
                .clickable {
                    emitIntent(ArchiveViewUiIntent.PathSelected(""))
                },
            text = archiveFileName,
            style = MaterialTheme.typography.headlineMedium,
            color = if (pathSegments.isEmpty()) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            }
        )

        // 路径段
        pathSegments.forEachIndexed { index, segment ->
            val accumulatedPath = pathSegments.take(index + 1).joinToString("/")

            Image(
                modifier = Modifier
                    .padding(horizontal = 2.5.dp)
                    .size(12.dp),
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .clickable {
                        emitIntent(ArchiveViewUiIntent.PathSelected(accumulatedPath))
                    },
                text = segment,
                style = MaterialTheme.typography.headlineMedium,
                color = if (pathSegments.size == index + 1) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                }
            )
        }
    }
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

/**
 * 网格布局的条目项
 */
@Composable
private fun GridEntryItem(
    entry: ArchiveEntry,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    val icon = if (entry.isDirectory) {
        painterResource(R.drawable.ic_folder)
    } else {
        painterResource(R.drawable.ic_file)
    }
    val secondaryText = if (!entry.isDirectory) entry.size.getReadableSize() else null

    AppGridFileItemCard(
        modifier = Modifier.fillMaxWidth(),
        icon = {
            Image(
                painter = icon,
                contentDescription = entry.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        },
        text = entry.name,
        secondaryText = secondaryText,
        onClick = {
            emitIntent(ArchiveViewUiIntent.EntrySelected(entry))
        }
    )
}

@Composable
private fun ExtractButton(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: ArchiveViewUiIntent) -> Unit = {}
) {
    Button(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        onClick = { emitIntent(ArchiveViewUiIntent.ExtractArchive) }
    ) {
        Text(stringResource(R.string.extract_archive))
    }
}

@Preview
@Composable
private fun ArchiveViewPreview() {
    ArchiveViewLayout(ArchiveViewUiState.Normal(archiveFile = File("archive.zip")))
}

@Composable
private fun DialogSwitch(
    dialogState: ArchiveViewDialogState
) {
    val coroutineScope = rememberCoroutineScope()
    when (dialogState) {
        ArchiveViewDialogState.None -> Unit

        is ArchiveViewDialogState.PasswordInput -> PasswordInputDialog(
            message = stringResource(R.string.enter_password_message, dialogState.file.name),
            onDismissRequest = {
                dialogState.deferredResult.complete(coroutineScope, null)
            },
            onConfirmRequest = {
                dialogState.deferredResult.complete(coroutineScope, it)
            }
        )
    }
}