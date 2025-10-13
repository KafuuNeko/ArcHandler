package cc.kafuu.archandler.feature.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.presentation.MainViewEvent
import cc.kafuu.archandler.feature.main.ui.MainViewBody
import cc.kafuu.archandler.libs.core.ViewEventCollector
import cc.kafuu.archandler.libs.core.CoreActivity
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.launch

class MainActivity : CoreActivity() {
    private val mViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(MainUiIntent.Init)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.emit(MainUiIntent.Resume)
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()

        val coroutineScope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        BackHandler {
            // 如果抽屉打开优先关闭抽屉
            if (drawerState.isOpen) {
                coroutineScope.launch { drawerState.close() }
                return@BackHandler
            }
            // 否则执行正常返回逻辑
            mViewModel.emit(MainUiIntent.Back)
        }

        ViewEventCollector(mViewModel) {
            if (it is MainViewEvent) onViewEvent(it)
        }

        LaunchedEffect(uiState) {
            if (uiState is MainUiState.Finished) finish()
        }

        MainViewBody(
            uiState = uiState,
            drawerState = drawerState,
            emitIntent = { intent -> mViewModel.emit(intent) }
        )
    }

    private fun onViewEvent(event: MainViewEvent) = when (event) {
        MainViewEvent.JumpFilePermissionSetting -> onJumpFilePermissionSetting()
    }

    private fun onJumpFilePermissionSetting() {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request { _: List<String?>?, _: Boolean -> mViewModel.emit(MainUiIntent.Init) }
    }
}