package cc.kafuu.archandler.feature.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.presentation.MainViewEvent
import cc.kafuu.archandler.feature.main.ui.MainLayout
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreActivityWithEvent
import cc.kafuu.archandler.libs.core.IViewEvent
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.launch
import kotlin.io.path.Path

class MainActivity : CoreActivityWithEvent() {
    private val mViewModel by viewModels<MainViewModel>()

    override fun getViewEventFlow() = mViewModel.viewEventFlow

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

        LaunchedEffect(uiState) {
            if (uiState is MainUiState.Finished) finish()
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            MainLayout(
                uiState = uiState,
                drawerState = drawerState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(MainUiIntent.Init)
        intent.getStringExtra(AppModel.KEY_USER_REDIRECT_PATH)?.run {
            mViewModel.emit(MainUiIntent.EntryUserDirectory(Path(this)))
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.emit(MainUiIntent.Resume)
    }

    override suspend fun onReceivedViewEvent(viewEvent: IViewEvent) {
        super.onReceivedViewEvent(viewEvent)
        when (viewEvent) {
            MainViewEvent.JumpFilePermissionSetting -> onJumpFilePermissionSetting()
        }
    }

    private fun onJumpFilePermissionSetting() {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request { _: List<String?>?, _: Boolean -> mViewModel.emit(MainUiIntent.Init) }
    }
}