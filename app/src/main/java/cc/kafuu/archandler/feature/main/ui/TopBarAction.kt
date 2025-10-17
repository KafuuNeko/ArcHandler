package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainListState
import cc.kafuu.archandler.feature.main.presentation.MainListViewModeState
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.feature.main.presentation.MainUiState

@Composable
fun TopBarAction(
    uiState: MainUiState.Accessible,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val listState = uiState.listState as? MainListState.Directory ?: return

    Image(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(24.dp)
            .clickable { expanded = true },
        painter = painterResource(R.drawable.ic_more_vert),
        contentDescription = null
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        when (uiState.viewModeState) {
            is MainListViewModeState.Pack,
            is MainListViewModeState.Paste -> Unit

            is MainListViewModeState.Normal -> {
                NormalSelectAction(emitIntent) { expanded = it }
            }

            is MainListViewModeState.MultipleSelect -> {
                MultipleSelectAction(listState, uiState.viewModeState, emitIntent) {
                    expanded = it
                }
            }
        }

        DropdownMenuItem(
            text = { Text(stringResource(R.string.create_directory)) },
            onClick = {
                emitIntent(MainUiIntent.CreateDirectoryClick)
                expanded = false
            }
        )
    }

}

@Composable
private fun NormalSelectAction(
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
    onSwitchExpanded: (Boolean) -> Unit
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.select_files)) },
        onClick = {
            emitIntent(MainUiIntent.FileMultipleSelectMode(enable = true))
            onSwitchExpanded(false)
        }
    )
}

@Composable
private fun MultipleSelectAction(
    listState: MainListState.Directory,
    viewMode: MainListViewModeState.MultipleSelect,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
    onSwitchExpanded: (Boolean) -> Unit
) {
    val isAllSelected = listState.files.size == viewMode.selected.size

    DropdownMenuItem(
        text = {
            Text(
                text = if (isAllSelected) {
                    stringResource(R.string.deselect)
                } else {
                    stringResource(R.string.select_all)
                }
            )
        },
        onClick = {
            emitIntent(if (isAllSelected) MainUiIntent.DeselectClick else MainUiIntent.SelectAllClick)
            onSwitchExpanded(false)
        }
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.select_all_no_duplicates)) },
        onClick = {
            emitIntent(MainUiIntent.SelectAllNoDuplicatesClick)
            onSwitchExpanded(false)
        }
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.invert_selection)) },
        onClick = {
            emitIntent(MainUiIntent.InvertSelectionClick)
            onSwitchExpanded(false)
        }
    )

    if (viewMode.selected.size == 1) {
        val file = viewMode.selected.first()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rename)) },
            onClick = {
                emitIntent(MainUiIntent.RenameClick(file))
                onSwitchExpanded(false)
            }
        )
    }

}
