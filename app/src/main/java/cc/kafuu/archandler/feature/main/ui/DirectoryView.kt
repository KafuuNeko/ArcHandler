package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import cc.kafuu.archandler.libs.extensions.getFileType
import cc.kafuu.archandler.libs.extensions.getLastModifiedDate
import cc.kafuu.archandler.libs.extensions.getReadableSize
import cc.kafuu.archandler.libs.model.FileType
import cc.kafuu.archandler.libs.model.LayoutType
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.utils.rememberVideoThumbnailPainter
import cc.kafuu.archandler.ui.widges.AppLazyColumn
import cc.kafuu.archandler.ui.widges.AppOptionalIconTextItemCard
import cc.kafuu.archandler.ui.widges.DirectoryPathBar
import cc.kafuu.archandler.ui.widges.AppGridFileItemCard
import cc.kafuu.archandler.ui.widges.AppLazyGridView
import cc.kafuu.archandler.ui.widges.IconMessageView
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun DirectoryView(
    modifier: Modifier = Modifier,
    loadState: MainLoadState,
    listState: MainListState.Directory,
    viewMode: MainListViewModeState,
    layoutType: LayoutType = LayoutType.LIST,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        DirectoryPathBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            storageData = listState.storageData,
            directoryPath = listState.directoryPath,
            onClickDevice = { MainUiIntent.ToStoragePage.also(emitIntent) }
        ) {
            MainUiIntent.FileSelected(listState.storageData, it).also(emitIntent)
        }

        val selectedSet = (viewMode as? MainListViewModeState.MultipleSelect)?.selected
        val isGridLayout = layoutType == LayoutType.GRID

        if (isGridLayout) {
            // 网格布局
            AppLazyGridView(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                state = listState.lazyGridState,
                items = listState.files,
                emptyView = { EmptyView(loadState) },
                gridItemContent = { file ->
                    GridFileItem(
                        storageData = listState.storageData,
                        file = file,
                        multipleSelectMode = selectedSet != null,
                        selectedSet = selectedSet,
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
                state = listState.lazyListState,
                items = listState.files,
                emptyState = { EmptyView(loadState) }
            ) { file ->
                ListFileItem(
                    storageData = listState.storageData,
                    file = file,
                    multipleSelectMode = selectedSet != null,
                    selectedSet = selectedSet,
                    emitIntent = emitIntent
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
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
private fun ListFileItem(
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
    val painter = file.getFileType().let { type ->
        val defaultIcon = painterResource(type.icon)
        when (type) {
            FileType.Image -> rememberAsyncImagePainter(model = file, placeholder = defaultIcon)
            FileType.Movie -> rememberVideoThumbnailPainter(model = file, placeholder = defaultIcon)
            else -> defaultIcon
        }
    }
    AppOptionalIconTextItemCard(
        modifier = Modifier
            .fillMaxWidth(),
        painter = painter,
        text = text,
        contentScale = ContentScale.Crop,
        checked = checked,
        secondaryText = secondaryText,
        displaySelectBox = multipleSelectMode,
        onLongClick = {
            emitIntent(MainUiIntent.FileMultipleSelectMode(!multipleSelectMode, file))
        }
    ) {
        MainUiIntent.FileSelected(
            storageData = storageData,
            file = file
        ).also(emitIntent)
    }
}

@Composable
private fun ColumnScope.EmptyView(loadState: MainLoadState) {
    if (loadState !is MainLoadState.None) {
        Spacer(modifier = Modifier.weight(1f))
        return
    }
    IconMessageView(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        icon = painterResource(R.drawable.ic_empty_folder),
        message = stringResource(R.string.empty_directory),
    )
}

/**
 * 网格布局的文件项
 */
@Composable
private fun GridFileItem(
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
    val painter = file.getFileType().let { type ->
        val defaultIcon = painterResource(type.icon)
        when (type) {
            FileType.Image -> rememberAsyncImagePainter(model = file, placeholder = defaultIcon)
            FileType.Movie -> rememberVideoThumbnailPainter(model = file, placeholder = defaultIcon)
            else -> defaultIcon
        }
    }

    AppGridFileItemCard(
        modifier = Modifier.fillMaxWidth(),
        icon = {
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = text,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        },
        text = text,
        secondaryText = secondaryText,
        selected = checked,
        showCheckbox = multipleSelectMode,
        onClick = {
            MainUiIntent.FileSelected(
                storageData = storageData,
                file = file
            ).also(emitIntent)
        },
        onLongClick = {
            emitIntent(MainUiIntent.FileMultipleSelectMode(!multipleSelectMode, file))
        }
    )
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
