package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.MainPackMenuEnum
import cc.kafuu.archandler.feature.main.model.MainPasteMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainLoadState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.BottomMenu
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppLazyColumn
import cc.kafuu.archandler.ui.widges.IconMessageView

@Composable
fun StorageVolumeView(
    modifier: Modifier = Modifier,
    loadState: MainLoadState,
    listState: MainListState.StorageVolume,
    viewMode: MainListViewModeState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.storage_volume),
            style = MaterialTheme.typography.headlineMedium
        )
        AppLazyColumn(
            modifier = Modifier
                .padding(top = 10.dp)
                .weight(1f),
            emptyState = {
                if (loadState !is MainLoadState.None) return@AppLazyColumn
                IconMessageView(
                    modifier = Modifier.fillMaxSize(),
                    icon = painterResource(R.drawable.ic_storage),
                    message = stringResource(R.string.no_accessible_storage_devices),
                )
            },
            items = listState.storageVolumes
        ) {
            AppIconTextItemCard(
                modifier = Modifier
                    .fillMaxWidth(),
                painter = painterResource(R.drawable.ic_storage),
                text = it.name,
                secondaryText = it.directory.path
            ) {
                emitIntent(MainUiIntent.StorageVolumeSelected(it))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        when (viewMode) {
            is MainListViewModeState.Paste -> {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                StoragePasteMenuView(
                    modifier = Modifier
                        .height(50.dp)
                        .padding(horizontal = 10.dp),
                    emitIntent = emitIntent
                )
            }

            is MainListViewModeState.Pack -> {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                StoragePackMenuView(
                    modifier = Modifier
                        .height(50.dp)
                        .padding(horizontal = 10.dp),
                    emitIntent = emitIntent
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun StoragePasteMenuView(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        BottomMenu(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = painterResource(MainPasteMenuEnum.Cancel.icon),
            title = stringResource(MainPasteMenuEnum.Cancel.title)
        ) {
            MainUiIntent.PasteMenuClick(menu = MainPasteMenuEnum.Cancel).also(emitIntent)
        }
    }
}

@Composable
private fun StoragePackMenuView(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        BottomMenu(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = painterResource(MainPackMenuEnum.Cancel.icon),
            title = stringResource(MainPackMenuEnum.Cancel.title)
        ) {
            MainUiIntent.PackMenuClick(menu = MainPackMenuEnum.Cancel).also(emitIntent)
        }
    }
}