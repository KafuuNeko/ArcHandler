package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPackMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPasteMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainLoadState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.BottomMenu
import cc.kafuu.archandler.feature.main.ui.common.IconMessageView
import cc.kafuu.archandler.libs.ext.getIcon
import cc.kafuu.archandler.libs.ext.getLastModifiedDate
import cc.kafuu.archandler.libs.ext.getReadableSize
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.widges.AppLazyColumn
import cc.kafuu.archandler.ui.widges.AppOptionalIconTextItemCard
import java.io.File
import java.nio.file.Path

@Composable
fun DirectoryView(
    modifier: Modifier = Modifier,
    loadState: MainLoadState,
    listState: MainListState.Directory,
    viewMode: MainListViewModeState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        DirectoryPathBar(
            listState.storageData, listState.directoryPath, emitIntent
        )

        AppLazyColumn(
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp)
                .weight(1f),
            items = listState.files,
            emptyState = {
                if (loadState !is MainLoadState.None) return@AppLazyColumn
                IconMessageView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    icon = painterResource(R.drawable.ic_empty_folder),
                    message = stringResource(R.string.empty_directory),
                )
            }
        ) { file ->
            val selectedSet = (viewMode as? MainListViewModeState.MultipleSelect)?.selected
            FileItem(
                storageData = listState.storageData,
                file = file,
                multipleSelectMode = selectedSet != null,
                selectedSet = selectedSet,
                emitIntent = emitIntent
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (viewMode !is MainListViewModeState.Normal) {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }

        when (viewMode) {
            MainListViewModeState.Normal -> Unit

            is MainListViewModeState.MultipleSelect -> MultipleMenuView(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 10.dp),
                listData = listState,
                viewMode = viewMode,
                emitIntent = emitIntent
            )

            is MainListViewModeState.Paste -> PasteMenuView(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                listState = listState,
                emitIntent = emitIntent
            )

            is MainListViewModeState.Pack -> PackMenuView(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                listState = listState,
                emitIntent = emitIntent
            )
        }
    }
}

@Composable
private fun DirectoryPathBar(
    storageData: StorageData,
    directoryPath: Path,
    emitIntent: (uiIntent: MainUiIntent) -> Unit,
) {
    val listState = rememberLazyListState()
    val baseURI = storageData.directory.toURI()
    val targetURI = File(directoryPath.toString()).toURI()
    val segments = baseURI.relativize(targetURI).path.takeIf {
        it.isNotEmpty()
    }?.trim('/')?.split('/') ?: emptyList()
    LaunchedEffect(segments, directoryPath) {
        if (segments.isNotEmpty()) listState.scrollToItem(segments.lastIndex)
    }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        state = listState,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                modifier = Modifier
                    .clickable {
                        emitIntent(MainUiIntent.FileSelected(storageData, storageData.directory))
                    },
                text = storageData.name,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        var parentFile = storageData.directory
        items(segments) {
            val currentFile = File(parentFile, it)
            Image(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(12.dp),
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .clickable {
                        emitIntent(MainUiIntent.FileSelected(storageData, currentFile))
                    },
                text = it,
                style = MaterialTheme.typography.headlineMedium
            )
            parentFile = currentFile
        }
    }

}

@Composable
private fun FileItem(
    storageData: StorageData,
    file: File,
    multipleSelectMode: Boolean,
    selectedSet: Set<File>?,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    val text = file.name
    val secondaryText = file.takeIf { it.isFile }?.let {
        stringResource(
            R.string.file_info_format,
            file.getLastModifiedDate(), file.getReadableSize()
        )
    }
    val checked = selectedSet?.contains(file) == true

    AppOptionalIconTextItemCard(
        modifier = Modifier
            .fillMaxWidth(),
        painter = painterResource(file.getIcon()),
        text = text,
        checked = checked,
        secondaryText = secondaryText,
        displaySelectBox = multipleSelectMode,
        onLongClick = { emitIntent(MainUiIntent.FileMultipleSelectMode(!multipleSelectMode)) }
    ) {
        MainUiIntent.FileSelected(
            storageData = storageData,
            file = file
        ).also(emitIntent)
    }
}

@Composable
private fun MultipleMenuView(
    modifier: Modifier = Modifier,
    listData: MainListState.Directory,
    viewMode: MainListViewModeState.MultipleSelect,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
    ) {
        val files = viewMode.selected.toList()
        MainMultipleMenuEnum.entries.forEach {
            BottomMenu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                MainUiIntent.MultipleMenuClick(
                    menu = it,
                    sourceStorageData = listData.storageData,
                    sourceDirectoryPath = listData.directoryPath,
                    sourceFiles = files,
                ).also(emitIntent)
            }
        }
    }
}

@Composable
private fun PasteMenuView(
    modifier: Modifier = Modifier,
    listState: MainListState.Directory,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        MainPasteMenuEnum.entries.forEach {
            BottomMenu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                MainUiIntent.PasteMenuClick(
                    menu = it,
                    targetStorageData = listState.storageData,
                    targetDirectoryPath = listState.directoryPath
                ).also(emitIntent)
            }
        }
    }
}

@Composable
private fun PackMenuView(
    modifier: Modifier = Modifier,
    listState: MainListState.Directory,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        MainPackMenuEnum.entries.forEach {
            BottomMenu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                MainUiIntent.PackMenuClick(
                    menu = it,
                    targetStorageData = listState.storageData,
                    targetDirectoryPath = listState.directoryPath
                ).also(emitIntent)
            }
        }
    }
}
