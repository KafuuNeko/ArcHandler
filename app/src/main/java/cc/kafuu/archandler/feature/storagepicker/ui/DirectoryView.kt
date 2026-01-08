package cc.kafuu.archandler.feature.storagepicker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerListState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerLoadState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiIntent
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
    loadState: StoragePickerLoadState,
    listState: StoragePickerListState.Directory,
    layoutType: LayoutType = LayoutType.LIST,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxSize()
    ) {
        DirectoryPathBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            storageData = listState.storageData,
            directoryPath = listState.directoryPath,
            onClickDevice = { StoragePickerUiIntent.ToStoragePage.also(emitIntent) }
        ) {
            StoragePickerUiIntent.FileSelected(listState.storageData, it).also(emitIntent)
        }

        val isGridLayout = layoutType == LayoutType.GRID

        if (isGridLayout) {
            // 网格布局
            AppLazyGridView(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                items = listState.files,
                emptyView = {
                    if (loadState !is StoragePickerLoadState.None) return@AppLazyGridView
                    IconMessageView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        icon = painterResource(R.drawable.ic_empty_folder),
                        message = stringResource(R.string.empty_directory),
                    )
                },
                gridItemContent = { file ->
                    GridFileItem(
                        storageData = listState.storageData,
                        file = file,
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
                items = listState.files,
                emptyState = {
                    if (loadState !is StoragePickerLoadState.None) return@AppLazyColumn
                    IconMessageView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        icon = painterResource(R.drawable.ic_empty_folder),
                        message = stringResource(R.string.empty_directory),
                    )
                }
            ) { file ->
                FileItem(
                    storageData = listState.storageData,
                    file = file,
                    emitIntent = emitIntent
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


@Composable
private fun FileItem(
    storageData: StorageData,
    file: File,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit = {},
) {
    val text = file.name
    val secondaryText = file.takeIf { it.isFile }?.let {
        stringResource(
            R.string.file_info_format,
            file.getLastModifiedDate(), file.getReadableSize()
        )
    }
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
        secondaryText = secondaryText,
    ) {
        StoragePickerUiIntent.FileSelected(
            storageData = storageData,
            file = file
        ).also(emitIntent)
    }
}

/**
 * 网格布局的文件项
 */
@Composable
private fun GridFileItem(
    storageData: StorageData,
    file: File,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit = {},
) {
    val text = file.name
    val secondaryText = file.takeIf { it.isFile }?.let {
        stringResource(
            R.string.file_info_format,
            file.getLastModifiedDate(), file.getReadableSize()
        )
    }
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
            Image(
                painter = painter,
                contentDescription = text,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        },
        text = text,
        secondaryText = secondaryText,
        onClick = {
            StoragePickerUiIntent.FileSelected(
                storageData = storageData,
                file = file
            ).also(emitIntent)
        }
    )
}


