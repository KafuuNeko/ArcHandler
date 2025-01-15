package cc.kafuu.archandler.feature.main.ui

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
import cc.kafuu.archandler.feature.main.presentation.MainListData
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.FillMessageView
import cc.kafuu.archandler.libs.model.LoadingState
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.LazyList

@Composable
fun StorageVolumeView(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    listData: MainListData.StorageVolume,
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
        LazyList(
            modifier = Modifier
                .padding(top = 10.dp),
            emptyState = {
                if (loadingState.isLoading) return@LazyList
                FillMessageView(
                    icon = painterResource(R.drawable.ic_storage),
                    message = stringResource(R.string.no_accessible_storage_devices),
                )
            },
            items = listData.storageVolumes
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
    }
}