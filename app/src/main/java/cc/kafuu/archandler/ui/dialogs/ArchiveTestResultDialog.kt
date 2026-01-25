package cc.kafuu.archandler.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cc.kafuu.archandler.R

@Composable
fun ArchiveTestResultDialog(
    success: Boolean,
    message: String,
    onDismissRequest: () -> Unit = {}
) {
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
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Image(
                modifier = Modifier.size(64.dp),
                painter = painterResource(
                    if (success) R.drawable.ic_check_circle else R.drawable.ic_error
                ),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = stringResource(
                    if (success) R.string.archive_test_success_title
                    else R.string.archive_test_failed_title
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // OK Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }
}
