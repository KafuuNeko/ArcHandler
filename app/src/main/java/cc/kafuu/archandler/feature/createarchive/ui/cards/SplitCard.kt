package cc.kafuu.archandler.feature.createarchive.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.ui.common.SectionCard
import cc.kafuu.archandler.libs.archive.model.SplitUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitCard(
    enabled: Boolean,
    unit: SplitUnit,
    number: Long?,
    onToggle: (Boolean) -> Unit,
    onUnitChange: (SplitUnit) -> Unit,
    onNumberChange: (Long?) -> Unit
) {
    SectionCard(stringResource(R.string.volume_compression)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Switch(checked = enabled, onCheckedChange = onToggle)
            Text(stringResource(if (enabled) R.string.enabled else R.string.disabled))
        }

        if (enabled) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val text = number?.takeIf { it > 0 }?.toString() ?: ""
                OutlinedTextField(
                    value = text,
                    onValueChange = { s ->
                        val v = s.filter { it.isDigit() }
                        onNumberChange(v.toLongOrNull())
                    },
                    label = { Text(stringResource(R.string.volume_size)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f),
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded, onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = unit.display,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.unit)) },
                        trailingIcon = {
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(120.dp)
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        SplitUnit.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(it.display) },
                                onClick = {
                                    expanded = false
                                    onUnitChange(it)
                                }
                            )
                        }
                    }
                }
            }

            ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                Text(stringResource(R.string.split_hint))
            }
        }
    }
}

@Preview
@Composable
private fun SplitCardPreview() {
    SplitCard(enabled = true, unit = SplitUnit.MB, number = null, {}, {}, {})
}