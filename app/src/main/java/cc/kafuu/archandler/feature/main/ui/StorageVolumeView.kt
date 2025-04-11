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
import cc.kafuu.archandler.feature.main.presentation.LoadState
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.ui.common.IconMessageView
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.LazyList

@Composable
fun StorageVolumeView(
    modifier: Modifier = Modifier,
    loadState: LoadState,
    listState: MainListState.StorageVolume,
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
                if (loadState !is LoadState.None) return@LazyList
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
    }
}