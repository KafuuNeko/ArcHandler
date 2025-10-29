package cc.kafuu.archandler.feature.storagepicker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.storagepicker.model.PickMode
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerListState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerLoadState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiIntent
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiState
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.libs.utils.TestUtils
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.theme.AppTheme
import cc.kafuu.archandler.ui.widges.AppPrimaryButton
import cc.kafuu.archandler.ui.widges.AppTopBar
import java.io.File
import kotlin.io.path.Path

@Composable
fun StoragePickerLayout(
    uiState: StoragePickerUiState,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit
) {
    BackHandler { emitIntent(StoragePickerUiIntent.Back) }
    when (uiState) {
        StoragePickerUiState.None, StoragePickerUiState.Finished -> Unit

        is StoragePickerUiState.Normal -> Box(modifier = Modifier.fillMaxSize()) {
            NormalView(uiState, emitIntent)
            LoadSwitch(uiState.loadState)
        }
    }
}

@Composable
private fun LoadSwitch(
    loadState: StoragePickerLoadState
) {
    when (loadState) {
        StoragePickerLoadState.None -> Unit

        StoragePickerLoadState.ExternalStoragesLoading -> {
            AppLoadDialog(message = stringResource(R.string.storage_loading_message))
        }

        StoragePickerLoadState.DirectoryLoading -> {
            AppLoadDialog(message = stringResource(R.string.directory_loading_message))
        }
    }
}

@Composable
private fun NormalView(
    uiState: StoragePickerUiState.Normal,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit
) {
    val title = when (uiState.pickMode) {
        PickMode.ChooseDirectory -> stringResource(R.string.select_folder_title)
    }
    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                modifier = Modifier,
                title = title,
                backIconPainter = painterResource(R.drawable.ic_close),
                onBack = { emitIntent(StoragePickerUiIntent.ClosePage) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val listState = uiState.listState) {
                StoragePickerListState.Undecided -> Unit

                is StoragePickerListState.StorageVolume -> StorageVolumeView(
                    modifier = Modifier.weight(1f),
                    loadState = uiState.loadState,
                    listState = listState,
                    emitIntent = emitIntent
                )

                is StoragePickerListState.Directory -> DirectoryView(
                    modifier = Modifier.weight(1f),
                    loadState = uiState.loadState,
                    listState = listState,
                    emitIntent = emitIntent
                )
            }
            SelectOption(uiState, emitIntent)
        }
    }
}

@Composable
private fun SelectOption(
    uiState: StoragePickerUiState.Normal,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit
) {
    when (uiState.pickMode) {
        PickMode.ChooseDirectory -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            AppPrimaryButton(
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.select_folder),
                enable = uiState.listState is StoragePickerListState.Directory
            ) {
                StoragePickerUiIntent.SelectionCompleted.also(emitIntent)
            }
        }
    }
}

@Preview(widthDp = 320, heightDp = 640, showBackground = true)
@Composable
private fun StoragePickerViewPreview() {
    AppTheme(dynamicColor = false) {
        StoragePickerLayout(
            uiState = StoragePickerUiState.Normal(
                listState = StoragePickerListState.Directory(
                    storageData = StorageData("test", File("/test/data")),
                    directoryPath = Path("/test/data"),
                    files = TestUtils.buildFileList()
                )
            )
        ) {}
    }
}