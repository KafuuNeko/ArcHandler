package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainListState.Directory
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.ui.scaffold.MainScaffoldDrawer
import cc.kafuu.archandler.feature.main.ui.scaffold.MainScaffoldTopBar
import cc.kafuu.archandler.libs.ext.castOrNull
import cc.kafuu.archandler.ui.widges.AppLoadingView
import kotlinx.coroutines.launch

@Composable
fun MainViewBody(
    uiState: MainUiState,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    when (uiState) {
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
    val title = uiState.viewModeState.castOrNull<MainListViewModeState.Pause>()?.let {
        stringResource(R.string.select_destination_path)
    } ?: when (val listData = uiState.listState) {
        is Directory -> {
            uiState.viewModeState.castOrNull<MainListViewModeState.MultipleSelect>()?.let {
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
                uiState.loadingState.takeIf {
                    it.isLoading
                }?.let {
                    AppLoadingView(loadingState = uiState.loadingState)
                }
            }
        }
    }
}



