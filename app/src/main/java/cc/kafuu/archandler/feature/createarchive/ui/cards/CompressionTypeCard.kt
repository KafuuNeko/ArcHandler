package cc.kafuu.archandler.feature.createarchive.ui.cards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.model.CompressionType
import cc.kafuu.archandler.feature.createarchive.ui.common.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionTypeCard(
    supportCompressionTypes: List<CompressionType>,
    compressionType: CompressionType,
    onFormatChange: (CompressionType) -> Unit
) {
    SectionCard(stringResource(R.string.compression_type)) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = stringResource(compressionType.displayName),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.compression_type)) },
                trailingIcon = {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                supportCompressionTypes.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(stringResource(format.displayName)) },
                        onClick = {
                            expanded = false
                            onFormatChange(format)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CompressionTypeCardPreview() {
    CompressionTypeCard(CompressionType.entries, CompressionType.Deflate) {}
}