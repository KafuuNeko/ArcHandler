package cc.kafuu.archandler.ui.dialogs

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R

@Composable
fun TextConfirmDialog(
    message: String,
    cancelContent: @Composable (RowScope.() -> Unit) = { Text(stringResource(R.string.cancel)) },
    confirmContent: @Composable (RowScope.() -> Unit) = { Text(stringResource(R.string.confirm)) },
    onDismissRequest: () -> Unit = {},
    onConfirmRequest: () -> Unit = {},
) {
    ConfirmDialog(
        cancelContent = cancelContent,
        confirmContent = confirmContent,
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onConfirmRequest
    ) {
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = message,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}