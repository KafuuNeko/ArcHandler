package cc.kafuu.archandler.feature.createarchive.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.ui.common.SectionCard
import kotlin.math.max

@Composable
fun LevelCard(
    range: IntRange,
    level: Int,
    onLevelChange: (Int) -> Unit
) {
    SectionCard(stringResource(R.string.compression_level)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Slider(
                value = level.toFloat(),
                onValueChange = { onLevelChange(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = max(0, (range.last - range.first) - 1)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${stringResource(R.string.low)}(${range.first})")
                AssistChip(
                    onClick = {},
                    label = { Text("${stringResource(R.string.current)}: $level") }
                )
                Text("${stringResource(R.string.high)}(${range.last})")
            }
        }
    }
}

@Preview
@Composable
fun LevelCardPreview() {
    LevelCard(0..9, 3) {}
}