package cc.kafuu.archandler.feature.storagepicker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerListState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerLoadState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiIntent
import cc.kafuu.archandler.libs.model.LayoutType
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppLazyColumn
import cc.kafuu.archandler.ui.widges.AppGridFileItemCard
import cc.kafuu.archandler.ui.widges.AppLazyGridView
import cc.kafuu.archandler.ui.widges.IconMessageView

@Composable
fun StorageVolumeView(
    modifier: Modifier = Modifier,
    loadState: StoragePickerLoadState,
    listState: StoragePickerListState.StorageVolume,
    layoutType: LayoutType = LayoutType.LIST,
    emitIntent: (uiIntent: StoragePickerUiIntent) -> Unit = {},
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

        val isGridLayout = layoutType == LayoutType.GRID

        if (isGridLayout) {
            // 网格布局
            AppLazyGridView(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f),
                items = listState.storageVolumes,
                emptyView = {
                    if (loadState !is StoragePickerLoadState.None) return@AppLazyGridView
                    IconMessageView(
                        modifier = Modifier.fillMaxSize(),
                        icon = painterResource(R.drawable.ic_storage),
                        message = stringResource(R.string.no_accessible_storage_devices),
                    )
                },
                gridItemContent = { storage ->
                    GridStorageItem(
                        storageData = storage,
                        onClick = { emitIntent(StoragePickerUiIntent.StorageVolumeSelected(storage)) }
                    )
                }
            )
        } else {
            // 列表布局
            AppLazyColumn(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f),
                emptyState = {
                    if (loadState !is StoragePickerLoadState.None) return@AppLazyColumn
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
                    emitIntent(StoragePickerUiIntent.StorageVolumeSelected(it))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * 网格布局的存储项
 */
@Composable
private fun GridStorageItem(
    storageData: StorageData,
    onClick: () -> Unit = {}
) {
    AppGridFileItemCard(
        modifier = Modifier.fillMaxWidth(),
        icon = {
            Image(
                painter = painterResource(R.drawable.ic_storage),
                contentDescription = storageData.name,
                modifier = Modifier.fillMaxSize()
            )
        },
        text = storageData.name,
        secondaryText = storageData.directory.path,
        onClick = onClick
    )
}
