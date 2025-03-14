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
import cc.kafuu.archandler.feature.about.AboutActivity
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainSingleEvent
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.ui.MainViewBody
import cc.kafuu.archandler.libs.core.CoreActivity
import cc.kafuu.archandler.libs.core.attachEventListener
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

        val validUiState = uiState ?: run {
            // ui状态为空，则发送初始化意图
            mViewModel.emit(MainUiIntent.Init)
            return
        }

        BackHandler {
            // 如果抽屉打开优先关闭抽屉
            if (drawerState.isOpen) {
                coroutineScope.launch { drawerState.close() }
                return@BackHandler
            }
            when (validUiState) {
                is MainUiState.Accessible -> onBackHandler(validUiState)
                else -> finish()
            }
        }

        MainViewBody(
            uiState = validUiState,
            drawerState = drawerState,
            emitIntent = { intent -> mViewModel.emit(intent) }
        )
    }

    private fun onSingleEvent(singleEvent: MainSingleEvent) = when (singleEvent) {
        MainSingleEvent.JumpFilePermissionSetting -> onJumpFilePermissionSetting()
        MainSingleEvent.JumpAboutPage -> AboutActivity.start(this)
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
                (state.listState as? MainListState.Directory)?.let {
                    doBackToParent(it)
                } ?: finish()
            }

            is MainListViewModeState.MultipleSelect -> mViewModel.emit(
                MainUiIntent.BackToNormalViewMode
            )

            is MainListViewModeState.Pause -> {
                (state.listState as? MainListState.Directory)?.let {
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