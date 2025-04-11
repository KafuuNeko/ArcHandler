package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cc.kafuu.archandler.feature.main.model.MainPasteMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.BottomMenu

@Composable
fun PasteMenuView(
    modifier: Modifier = Modifier,
    listState: MainListState.Directory,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        MainPasteMenuEnum.entries.forEach {
            BottomMenu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                MainUiIntent.PasteMenuClick(
                    menu = it,
                    targetStorageData = listState.storageData,
                    targetDirectoryPath = listState.directoryPath
                ).also(emitIntent)
            }
        }
    }
}

