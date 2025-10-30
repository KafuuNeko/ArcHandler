package cc.kafuu.archandler.feature.createarchive.ui.cards

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.ui.common.SectionCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePathCard(
    dir: File,
    outputName: String,
    onNameChange: (String) -> Unit,
    onSelectFolder: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(dir.path) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    SectionCard(stringResource(R.string.save_location)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onSelectFolder
            ) {
                Text(text = stringResource(R.string.select_folder))
            }
            Text(
                modifier = Modifier
                    .horizontalScroll(scrollState),
                text = dir.path.ifBlank { stringResource(R.string.text_not_selected) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        OutlinedTextField(
            value = outputName,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.output_filename_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {})
        )
    }
}

@Preview
@Composable
private fun SavePathCardPreview() {
    SavePathCard(File(""), "test", {})
}