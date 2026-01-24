package cc.kafuu.archandler.feature.duplicatefinder.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderLoadState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderSearchState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderUiIntent
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderUiState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFileGroup
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.dialogs.TextConfirmDialog
import cc.kafuu.archandler.ui.widges.AppPrimaryButton
import cc.kafuu.archandler.ui.widges.IconMessageView
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateFinderLayout(
    uiState: DuplicateFinderUiState,
    emitIntent: (DuplicateFinderUiIntent) -> Unit = {}
) {
    when (uiState) {
        DuplicateFinderUiState.None -> Unit

        is DuplicateFinderUiState.Finished -> Unit

        is DuplicateFinderUiState.Normal -> {
            NormalView(
                uiState = uiState,
                emitIntent = emitIntent
            )
            LoadDialogSwitch(uiState.loadState, emitIntent)
            DialogSwitch(uiState.dialogState, emitIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalView(
    uiState: DuplicateFinderUiState.Normal,
    emitIntent: (DuplicateFinderUiIntent) -> Unit
) {
    val title = when {
        uiState.selectedFiles.isNotEmpty() -> stringResource(R.string.n_files_selected, uiState.selectedFiles.size)
        uiState.searchState is DuplicateFinderSearchState.Success -> {
            val state = uiState.searchState
            stringResource(R.string.duplicate_files_found, state.duplicateFileCount)
        }
        else -> stringResource(R.string.duplicate_finder)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            emitIntent(DuplicateFinderUiIntent.Back)
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState.selectedFiles.isNotEmpty()) {
                BottomActionBar(uiState, emitIntent)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val searchState = uiState.searchState) {
                is DuplicateFinderSearchState.Idle,
                is DuplicateFinderSearchState.Searching -> Unit

                is DuplicateFinderSearchState.Success -> {
                    SuccessView(
                        searchState = searchState,
                        selectedFiles = uiState.selectedFiles,
                        emitIntent = emitIntent
                    )
                }

                is DuplicateFinderSearchState.Error -> {
                    ErrorView(searchState.message)
                }
            }
        }
    }
}

@Composable
private fun SuccessView(
    searchState: DuplicateFinderSearchState.Success,
    selectedFiles: Set<File>,
    emitIntent: (DuplicateFinderUiIntent) -> Unit
) {
    if (searchState.duplicateGroups.isEmpty()) {
        IconMessageView(
            modifier = Modifier.fillMaxSize(),
            icon = painterResource(R.drawable.ic_check_circle),
            message = stringResource(R.string.no_duplicate_files_found)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 统计信息
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.search_summary),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.search_summary_details,
                                searchState.totalFiles,
                                searchState.duplicateFileCount,
                                searchState.duplicateGroups.size,
                                searchState.wastedSpace.getReadableSize()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 重复文件组
            items(searchState.duplicateGroups) { group ->
                DuplicateGroupCard(
                    group = group,
                    selectedFiles = selectedFiles,
                    emitIntent = emitIntent
                )
            }
        }
    }
}

@Composable
private fun DuplicateGroupCard(
    group: DuplicateFileGroup,
    selectedFiles: Set<File>,
    emitIntent: (DuplicateFinderUiIntent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 组标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.duplicate_group_title,
                            group.files.size,
                            group.fileSize.getReadableSize()
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = group.hash,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 文件列表
            group.files.forEach { file ->
                DuplicateFileItem(
                    modifier = Modifier.padding(vertical = 4.dp),
                    file = file,
                    selected = selectedFiles.contains(file),
                    onClick = {
                        // 点击文件暂时不做任何操作
                    },
                    onSelectionChange = { file, selected ->
                        emitIntent(DuplicateFinderUiIntent.FileSelect(file, selected))
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    IconMessageView(
        modifier = Modifier.fillMaxSize(),
        icon = painterResource(R.drawable.ic_error),
        message = message
    )
}

@Composable
private fun BottomActionBar(
    uiState: DuplicateFinderUiState.Normal,
    emitIntent: (DuplicateFinderUiIntent) -> Unit
) {
    Column {
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppPrimaryButton(
                modifier = Modifier.weight(1f),
                onClick = { emitIntent(DuplicateFinderUiIntent.DeselectAll) },
                text = stringResource(R.string.deselect_all)
            )

            Spacer(modifier = Modifier.width(8.dp))

            AppPrimaryButton(
                modifier = Modifier.weight(1f),
                onClick = { emitIntent(DuplicateFinderUiIntent.DeleteSelected) },
                text = stringResource(R.string.delete_selected, uiState.selectedFiles.size)
            )
        }
    }
}

@Composable
private fun LoadDialogSwitch(
    loadState: DuplicateFinderLoadState,
    emitIntent: (DuplicateFinderUiIntent) -> Unit
) {
    when (loadState) {
        is DuplicateFinderLoadState.None -> Unit

        is DuplicateFinderLoadState.Scanning -> {
            val messages = buildList {
                add(stringResource(R.string.scanning_files_message))
                loadState.currentFile?.let {
                    add(it.name)
                }
                if (loadState.scannedCount > 0) {
                    add(stringResource(R.string.scanned_directories, loadState.scannedCount))
                }
            }
            AppLoadDialog(
                messages = messages,
                buttonText = stringResource(R.string.cancel),
                onClickButton = { emitIntent(DuplicateFinderUiIntent.CancelSearch) }
            )
        }

        is DuplicateFinderLoadState.Hashing -> {
            val messages = buildList {
                add(stringResource(R.string.calculating_hash))
                add(loadState.currentFile.name)
                add("${loadState.processedCount}/${loadState.totalCount}")
            }
            AppLoadDialog(
                messages = messages,
                buttonText = stringResource(R.string.cancel),
                onClickButton = { emitIntent(DuplicateFinderUiIntent.CancelSearch) }
            )
        }

        is DuplicateFinderLoadState.Deleting -> {
            val messages = listOf(
                stringResource(R.string.files_deleting),
                "${loadState.deletedCount}/${loadState.totalCount}"
            )
            AppLoadDialog(messages = messages)
        }
    }
}

@Composable
private fun DialogSwitch(
    dialogState: cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderDialogState,
    emitIntent: (DuplicateFinderUiIntent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    when (dialogState) {
        is cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderDialogState.None -> Unit

        is cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderDialogState.DeleteConfirm -> {
            TextConfirmDialog(
                message = if (dialogState.fileSet.size > 1) {
                    stringResource(R.string.delete_files_message, dialogState.fileSet.size)
                } else {
                    stringResource(
                        R.string.delete_file_message,
                        dialogState.fileSet.firstOrNull()?.name ?: ""
                    )
                },
                confirmContent = {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onDismissRequest = {
                    dialogState.deferredResult.complete(coroutineScope, false)
                },
                onConfirmRequest = {
                    dialogState.deferredResult.complete(coroutineScope, true)
                }
            )
        }
    }
}

private fun Long.getReadableSize(): String {
    if (this == 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    val unitIndex = (kotlin.math.log10(this.toDouble()) / kotlin.math.log10(1024.0)).toInt()
        .coerceAtMost(units.size - 1)
    val readableSize = this / 1024.0.pow(unitIndex.toDouble())
    return "%.2f %s".format(readableSize, units[unitIndex])
}
