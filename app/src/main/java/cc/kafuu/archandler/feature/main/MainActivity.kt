package cc.kafuu.archandler.feature.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainSingleEvent
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.ui.MainViewBody
import cc.kafuu.archandler.libs.core.CoreActivity
import cc.kafuu.archandler.libs.core.attachEventListener
import cc.kafuu.archandler.libs.ext.castOrNull
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.launch

class MainActivity : CoreActivity() {
    private val mViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachEventListener(mViewModel) { onSingleEvent(it) }
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        uiState?.also { state ->
            BackHandler {
                // 如果抽屉打开优先关闭抽屉
                if (drawerState.isOpen) {
                    coroutineScope.launch { drawerState.close() }
                    return@BackHandler
                }
                when (state) {
                    is MainUiState.Accessible -> onBackHandler(state)
                    else -> finish()
                }
            }
            MainViewBody(
                uiState = state,
                drawerState = drawerState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        } ?: mViewModel.emit(MainUiIntent.Init)
    }

    private fun onSingleEvent(singleEvent: MainSingleEvent) = when (singleEvent) {
        MainSingleEvent.JumpFilePermissionSetting -> onJumpFilePermissionSetting()
    }

    private fun onJumpFilePermissionSetting() {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request { _: List<String?>?, _: Boolean -> mViewModel.emit(MainUiIntent.Init) }
    }

    private fun onBackHandler(state: MainUiState.Accessible) {
        if (state.loadingState.isLoading) return

        when (state.viewModeState) {
            MainListViewModeState.Normal -> {
                state.listState.castOrNull<MainListState.Directory>()?.let {
                    doBackToParent(it)
                } ?: finish()
            }

            is MainListViewModeState.MultipleSelect -> mViewModel.emit(
                MainUiIntent.BackToNormalViewMode
            )

            is MainListViewModeState.Pause -> {
                state.listState.castOrNull<MainListState.Directory>()?.let {
                    doBackToParent(it)
                } ?: mViewModel.emit(MainUiIntent.BackToNormalViewMode)
            }
        }
    }

    private fun doBackToParent(listState: MainListState.Directory) {
        MainUiIntent.BackToParent(
            storageData = listState.storageData,
            currentPath = listState.directoryPath
        ).also {
            mViewModel.emit(it)
        }
    }
}