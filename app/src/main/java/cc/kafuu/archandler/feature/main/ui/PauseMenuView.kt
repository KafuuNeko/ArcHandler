package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent

@Composable
fun PauseMenuView(
    modifier: Modifier = Modifier,
    viewMode: MainListViewModeState.Pause,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        // TODO: 待实现粘贴模式菜单功能
    }
}

