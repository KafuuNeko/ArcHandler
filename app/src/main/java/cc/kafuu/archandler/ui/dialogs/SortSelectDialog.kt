package cc.kafuu.archandler.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.SortType

@Composable
fun SortSelectDialog(
    currentSortType: SortType,
    onDismissRequest: () -> Unit = {},
    onConfirmRequest: (SortType) -> Unit = {},
) {
    var selectedSortType by remember { mutableIntStateOf(currentSortType.value) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(20.5.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sort),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SortType.entries.forEach { sortType ->
                SortOptionItem(
                    sortType = sortType,
                    selected = selectedSortType == sortType.value,
                    onClick = { selectedSortType = sortType.value }
                )
            }

            ActionButtons(
                onDismissRequest = onDismissRequest,
                onConfirmRequest = {
                    onConfirmRequest(SortType.fromValue(selectedSortType))
                }
            )
        }
    }
}

@Composable
private fun SortOptionItem(
    sortType: SortType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = sortType.getTitle(androidx.compose.ui.platform.LocalContext.current),
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ActionButtons(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismissRequest) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(
            onClick = onConfirmRequest,
            modifier = Modifier.width(80.dp)
        ) {
            Text(stringResource(R.string.confirm))
        }
    }
}
