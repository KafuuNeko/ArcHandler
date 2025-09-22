package cc.kafuu.archandler.feature.createarchive.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.ui.common.SectionCard

@Composable
fun PasswordCard(
    password: String,
    onPasswordChange: (String) -> Unit,
    onClear: () -> Unit
) {
    SectionCard(stringResource(R.string.encryption)) {
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            label = { Text(stringResource(R.string.password_optional)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onClear() }) { Text(stringResource(R.string.clear)) }
        }
    }
}

@Preview
@Composable
private fun PasswordCardPreview() {
    PasswordCard(password = "123456", {}, {})
}