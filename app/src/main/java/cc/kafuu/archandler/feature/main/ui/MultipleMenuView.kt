package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cc.kafuu.archandler.feature.main.model.MainMultipleMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainListData
import cc.kafuu.archandler.feature.main.presentation.MainListViewMode
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.BottomMenu

@Composable
fun MultipleMenuView(
    modifier: Modifier = Modifier,
    listData: MainListData.Directory,
    viewMode: MainListViewMode.MultipleSelect,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
    ) {
        val files = viewMode.selected.toList()
        MainMultipleMenuEnum.entries.forEach {
            BottomMenu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                MainUiIntent.MultipleMenuClick(
                    menu = it,
                    sourceStorageData = listData.storageData,
                    sourceDirectoryPath = listData.directoryPath,
                    sourceFiles = files,
                ).also(emitIntent)
            }
        }
    }
}

