package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.FillMessageView
import cc.kafuu.archandler.libs.ext.castOrNull
import cc.kafuu.archandler.libs.ext.getIcon
import cc.kafuu.archandler.libs.ext.getLastModifiedDate
import cc.kafuu.archandler.libs.ext.getReadableSize
import cc.kafuu.archandler.feature.main.presentation.LoadingState
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.widges.AppOptionalIconTextItemCard
import cc.kafuu.archandler.ui.widges.LazyList
import java.io.File

@Composable
fun DirectoryView(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    listState: MainListState.Directory,
    viewMode: MainListViewModeState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 10.dp),
            text = listState.directoryPath.toString(),
            style = MaterialTheme.typography.headlineMedium
        )

        LazyList(
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp)
                .weight(1f),
            items = listState.files,
            emptyState = {
                if (loadingState.isLoading) return@LazyList
                FillMessageView(
                    icon = painterResource(R.drawable.ic_empty_folder),
                    message = stringResource(R.string.empty_directory),
                )
            }
        ) { file ->
            val selectedSet = viewMode.castOrNull<MainListViewModeState.MultipleSelect>()?.selected
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

            is MainListViewModeState.Pause -> PauseMenuView(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                viewMode = viewMode,
                emitIntent = emitIntent
            )
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
    val checked = selectedSet?.contains(file) ?: false
    val onCheckedChange = {
        MainUiIntent.FileCheckedChange(
            file = file,
            checked = !checked
        ).also(emitIntent)
    }
    AppOptionalIconTextItemCard(
        modifier = Modifier
            .fillMaxWidth(),
        painter = painterResource(file.getIcon()),
        text = text,
        checked = checked,
        secondaryText = secondaryText,
        displaySelectBox = multipleSelectMode,
        onCheckedChange = { onCheckedChange() },
        onLongClick = { emitIntent(MainUiIntent.FileMultipleSelectMode(!multipleSelectMode)) }
    ) {
        if (multipleSelectMode) {
            onCheckedChange()
            return@AppOptionalIconTextItemCard
        }
        MainUiIntent.FileSelected(
            storageData = storageData,
            file = file
        ).also(emitIntent)
    }
}

