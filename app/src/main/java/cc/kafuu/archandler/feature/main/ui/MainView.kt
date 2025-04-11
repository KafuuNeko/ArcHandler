package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.ui.scaffold.MainScaffoldDrawer
import cc.kafuu.archandler.feature.main.ui.scaffold.MainScaffoldTopBar
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.libs.utils.TestUtils
import cc.kafuu.archandler.ui.theme.AppTheme
import cc.kafuu.archandler.ui.widges.AppLoadSwitch
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.path.Path

@Composable
fun MainViewBody(
    uiState: MainUiState,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    when (uiState) {
        MainUiState.None, MainUiState.Finished -> Unit

        MainUiState.PermissionDenied -> PermissionDeniedView(
            emitIntent = emitIntent
        )

        is MainUiState.Accessible -> MainLayout(
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
}


@Composable
private fun MainLayout(
    uiState: MainUiState.Accessible,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val title = (uiState.viewModeState as? MainListViewModeState.Paste)?.let {
        stringResource(R.string.select_destination_path)
    } ?: when (val listData = uiState.listState) {
        is MainListState.Directory -> {
            (uiState.viewModeState as? MainListViewModeState.MultipleSelect)?.let {
                stringResource(R.string.n_files_selected, it.selected.size)
            } ?: listData.storageData.name
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
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                content()
                AppLoadSwitch(loadState = uiState.loadState)
            }
        }
    }
}

@Preview(widthDp = 320, heightDp = 640, showBackground = true)
@Composable
private fun PermissionDeniedPreview() {
    AppTheme(dynamicColor = false) {
        MainViewBody(
            uiState = MainUiState.PermissionDenied,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun StorageVolumePreview() {
    AppTheme(dynamicColor = false) {
        MainViewBody(
            uiState = MainUiState.Accessible(
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
        MainViewBody(
            uiState = MainUiState.Accessible(
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

