package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState
import cc.kafuu.archandler.feature.main.ui.common.MoreMenu
import cc.kafuu.archandler.libs.model.LayoutType

@Composable
fun TopBarAction(
    uiState: MainUiState.Normal,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    // 布局切换按钮
    Image(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(24.dp)
            .clickable {
                emitIntent(
                    MainUiIntent.SwitchLayoutType(uiState.layoutType.toggle())
                )
            },
        painter = painterResource(
            if (uiState.layoutType == LayoutType.LIST) {
                R.drawable.ic_grid_view
            } else {
                R.drawable.ic_list_view
            }
        ),
        contentDescription = stringResource(R.string.switch_layout_type)
    )

    // 更多菜单
    MoreMenu(
        modifier = Modifier.padding(horizontal = 10.dp),
        listState = uiState.listState,
        viewModeState = uiState.viewModeState,
        emitIntent = emitIntent
    ) {
        Image(
            modifier = Modifier
                .size(30.dp)
                .clickable(onClick = it),
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = stringResource(R.string.more)
        )
    }
}
