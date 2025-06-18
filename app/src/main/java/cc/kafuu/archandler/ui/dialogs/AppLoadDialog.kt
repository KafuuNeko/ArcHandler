package cc.kafuu.archandler.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cc.kafuu.archandler.ui.theme.AppTheme

@Composable
fun AppLoadDialog(
    messages: List<String>,
    onDismissRequest: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        DialogView(messages)
    }
}

@Composable
fun AppLoadDialog(
    message: String,
    onDismissRequest: () -> Unit = {},
) {
    AppLoadDialog(listOf(message), onDismissRequest)
}

@Composable
private fun DialogView(
    messages: List<String>
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .requiredSizeIn(minHeight = 135.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.5.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        messages.forEach {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun DialogViewPreview() {
    AppTheme(dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DialogView(listOf("Copying...", "1/32"))
        }
    }
}