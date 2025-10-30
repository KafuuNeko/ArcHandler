package cc.kafuu.archandler.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cc.kafuu.archandler.R
import cc.kafuu.archandler.ui.theme.AppTheme

@Composable
fun ConfirmDialog(
    cancelContent: @Composable (RowScope.() -> Unit) = { Text(stringResource(R.string.cancel)) },
    confirmContent: @Composable (RowScope.() -> Unit) = { Text(stringResource(R.string.confirm)) },
    onDismissRequest: () -> Unit = {},
    onConfirmRequest: () -> Unit = {},
    content: @Composable ()-> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        DialogView(content, cancelContent, confirmContent, onDismissRequest, onConfirmRequest)
    }
}

@Composable
private fun DialogView(
    content: @Composable ()-> Unit,
    cancelContent: @Composable (RowScope.() -> Unit),
    confirmContent: @Composable (RowScope.() -> Unit),
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.5.dp)
            )
            .padding(10.dp)
    ) {
        content()
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest, content = cancelContent)
            Spacer(modifier = Modifier.width(10.dp))
            TextButton(onClick = onConfirmRequest, content = confirmContent)
        }
    }
}


@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun DialogViewPreview() {
    AppTheme(dynamicColor = false) {
        ConfirmDialog(
            { Text(stringResource(R.string.cancel)) },
            { Text(stringResource(R.string.delete), color = Color.Red) },
            {}, {}
        ) {
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "是否删除xxx.zip",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}