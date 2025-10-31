package cc.kafuu.archandler.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.extensions.getLastModifiedDate
import cc.kafuu.archandler.libs.extensions.getReadableSize
import cc.kafuu.archandler.libs.model.FileConflictStrategy
import cc.kafuu.archandler.ui.theme.AppTheme
import java.io.File

@Composable
fun FileConflictDialog(
    oldFile: File, newFile: File,
    onCancel: () -> Unit = {},
    onSelected: (strategy: FileConflictStrategy, applyToAllConflicts: Boolean) -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        DialogView(oldFile, newFile, onSelected)
    }
}

@Composable
private fun DialogView(
    oldFile: File, newFile: File,
    onSelected: (strategy: FileConflictStrategy, applyToAllConflicts: Boolean) -> Unit,
) {
    val oldName = remember(oldFile) { oldFile.name }
    val newName = remember(newFile) { newFile.name }
    val oldSize = remember(oldFile) { oldFile.getReadableSize() }
    val newSize = remember(newFile) { newFile.getReadableSize() }
    val oldModified = remember(oldFile) { oldFile.getLastModifiedDate() }
    val newModified = remember(newFile) { newFile.getLastModifiedDate() }

    var applyToAllConflicts by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(R.string.file_conflict_message_message),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.existing_file_label),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = oldName,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(R.string.size_colon, oldSize),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(R.string.modified_colon, oldModified),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.new_file_label),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = newName,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(R.string.size_colon, newSize),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(R.string.modified_colon, newModified),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .clickable { applyToAllConflicts = !applyToAllConflicts },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(22.dp),
                painter = painterResource(if (applyToAllConflicts) R.drawable.ic_check_circle else R.drawable.ic_circle),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.do_this_for_all_conflicts),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    onSelected(FileConflictStrategy.Skip, applyToAllConflicts)
                }
            ) {
                Text(text = stringResource(R.string.skip))
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    onSelected(FileConflictStrategy.KeepBoth, applyToAllConflicts)
                }
            ) {
                Text(text = stringResource(R.string.keep_both))
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    onSelected(FileConflictStrategy.Overwrite, applyToAllConflicts)
                }
            ) {
                Text(text = stringResource(R.string.overwrite))
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun DialogViewPreview() {
    AppTheme(dynamicColor = false) {
        FileConflictDialog(
            oldFile = File("aaaa.jpg"),
            newFile = File("aaaa.jpg"),
        ) { _, _ -> }
    }
}
