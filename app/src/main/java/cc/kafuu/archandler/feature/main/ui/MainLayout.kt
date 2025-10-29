package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainDialogState
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainLoadState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.ui.scaffold.MainScaffoldDrawer
import cc.kafuu.archandler.feature.main.ui.scaffold.MainScaffoldTopBar
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.libs.utils.TestUtils
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.dialogs.ConfirmDialog
import cc.kafuu.archandler.ui.dialogs.PasswordInputDialog
import cc.kafuu.archandler.ui.theme.AppTheme
import cc.kafuu.archandler.ui.widges.AppPrimaryButton
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.name

@Composable
fun MainLayout(
    uiState: MainUiState,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    when (uiState) {
        MainUiState.None, MainUiState.Finished -> Unit

        is MainUiState.PermissionDenied -> PermissionDeniedView(emitIntent = emitIntent)

        is MainUiState.Normal -> {
            NormalView(uiState, drawerState, emitIntent)
            MainLoadDialogSwitch(uiState.loadState, emitIntent)
            uiState.dialogStates.firstOrNull()?.run { MainDialogSwitch(this) }
        }
    }
}

@Composable
private fun MainLoadDialogSwitch(
    loadState: MainLoadState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit,
) {
    when (loadState) {
        MainLoadState.None -> Unit

        MainLoadState.ExternalStoragesLoading -> {
            AppLoadDialog(message = stringResource(R.string.storage_loading_message))
        }

        MainLoadState.DirectoryLoading -> {
            AppLoadDialog(message = stringResource(R.string.directory_loading_message))
        }

        is MainLoadState.Pasting -> {
            val message = stringResource(
                if (loadState.isMoving) R.string.moving_message else R.string.copying_message
            )
            val progress = "${loadState.quantityCompleted + 1}/${loadState.totality}"
            AppLoadDialog(messages = listOf(message, progress))
        }

        is MainLoadState.ArchiveOpening -> {
            val message = stringResource(R.string.opening_archive_message)
            AppLoadDialog(messages = listOf(message, loadState.file.name))
        }

        is MainLoadState.Unpacking -> {
            val message = stringResource(R.string.unpacking_file_message)
            val filename = File(loadState.path).name
            val progress = "${loadState.index}/${loadState.target}"
            AppLoadDialog(
                messages = listOf(message, filename, progress),
                buttonText = stringResource(R.string.cancel),
                onClickButton = {
                    emitIntent(MainUiIntent.CancelUnpackingJob)
                }
            )
        }

        is MainLoadState.FilesDeleting -> {
            val message = stringResource(R.string.files_deleting)
            AppLoadDialog(messages = listOf(message, loadState.file.name))
        }

        is MainLoadState.QueryDuplicateFiles -> {
            val message = stringResource(R.string.query_duplicate_files_message)
            AppLoadDialog(
                messages = listOf(
                    message,
                    loadState.file?.name ?: stringResource(R.string.waiting)
                ),
                buttonText = stringResource(R.string.cancel),
                onClickButton = {
                    emitIntent(MainUiIntent.CancelSelectNoDuplicatesJob)
                }
            )
        }
    }
}

@Composable
private fun MainDialogSwitch(
    dialogState: MainDialogState
) {
    val coroutineScope = rememberCoroutineScope()
    when (dialogState) {
        is MainDialogState.PasswordInput -> PasswordInputDialog(
            message = stringResource(R.string.enter_password_message, dialogState.file.name),
            onDismissRequest = {
                dialogState.deferredResult.complete(coroutineScope, null)
            },
            onConfirmRequest = {
                dialogState.deferredResult.complete(coroutineScope, it)
            }
        )

        is MainDialogState.FileDeleteConfirm -> ConfirmDialog(
            message = if (dialogState.fileSet.size > 1) {
                stringResource(R.string.delete_files_message, dialogState.fileSet.size)
            } else {
                stringResource(
                    R.string.delete_file_message, dialogState.fileSet.firstOrNull()?.name ?: ""
                )
            },
            confirmContent = { Text(stringResource(R.string.delete), color = Color.Red) },
            onDismissRequest = {
                dialogState.deferredResult.complete(coroutineScope, false)
            },
            onConfirmRequest = {
                dialogState.deferredResult.complete(coroutineScope, true)
            }
        )
    }
}

@Composable
fun PermissionDeniedView(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(64.dp),
            painter = painterResource(R.drawable.ic_folder_off),
            contentDescription = stringResource(R.string.not_permission_in_main_view)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.not_permission_in_main_view),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                emitIntent(MainUiIntent.JumpFilePermissionSetting)
            },
            text = stringResource(R.string.enable_permission),
        )
    }
}

@Composable
private fun NormalView(
    uiState: MainUiState.Normal,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    MainViewBody(
        uiState = uiState,
        drawerState = drawerState,
        emitIntent = emitIntent
    ) {
        AccessibleView(
            uiState = uiState,
            emitIntent = emitIntent
        )
    }
}

@Composable
private fun MainViewBody(
    uiState: MainUiState.Normal,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val title = when (uiState.viewModeState) {
        is MainListViewModeState.Pack,
        is MainListViewModeState.Paste -> stringResource(R.string.select_destination_path)

        else -> null
    } ?: when (val listData = uiState.listState) {
        is MainListState.Directory -> {
            (uiState.viewModeState as? MainListViewModeState.MultipleSelect)?.let {
                stringResource(R.string.n_files_selected, it.selected.size)
            } ?: if (listData.storageData.directory.path == listData.directoryPath.toString()) {
                listData.storageData.name
            } else {
                listData.directoryPath.name
            }
        }

        else -> stringResource(R.string.app_name)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainScaffoldDrawer(
                drawerState = drawerState,
                emitIntent = emitIntent
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .statusBarsPadding(),
            topBar = {
                MainScaffoldTopBar(
                    title = title,
                    actions = { TopBarAction(uiState, emitIntent) },
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) { content() }
        }
    }
}

@Composable
private fun AccessibleView(
    modifier: Modifier = Modifier,
    uiState: MainUiState.Normal,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    when (val listState = uiState.listState) {
        MainListState.Undecided -> Unit

        is MainListState.StorageVolume -> StorageVolumeView(
            modifier = modifier,
            loadState = uiState.loadState,
            listState = listState,
            viewMode = uiState.viewModeState,
            emitIntent = emitIntent
        )

        is MainListState.Directory -> DirectoryView(
            modifier = modifier,
            loadState = uiState.loadState,
            listState = listState,
            viewMode = uiState.viewModeState,
            emitIntent = emitIntent
        )
    }
}


@Preview(widthDp = 320, heightDp = 640, showBackground = true)
@Composable
private fun PermissionDeniedPreview() {
    AppTheme(dynamicColor = false) {
        MainLayout(
            uiState = MainUiState.PermissionDenied,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun StorageVolumePreview() {
    AppTheme(dynamicColor = false) {
        MainLayout(
            uiState = MainUiState.Normal(
                viewModeState = MainListViewModeState.Normal,
                listState = MainListState.StorageVolume(
                    storageVolumes = TestUtils.buildStorageDataList(100)
                )
            ),
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun DirectoryPreview() {
    AppTheme(dynamicColor = false) {
        MainLayout(
            uiState = MainUiState.Normal(
                viewModeState = MainListViewModeState.Normal,
                listState = MainListState.Directory(
                    storageData = StorageData("test", File("/test/data")),
                    directoryPath = Path("/test/data"),
                    files = TestUtils.buildFileList()
                )
            ),
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

