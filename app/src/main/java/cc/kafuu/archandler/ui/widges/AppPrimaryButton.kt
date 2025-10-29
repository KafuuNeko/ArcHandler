package cc.kafuu.archandler.ui.widges

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun AppPrimaryButton(
    modifier: Modifier,
    text: String,
    enable: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.displayMedium,
    onClick: () -> Unit = {},
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enable,
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = textStyle
        )
    }
}