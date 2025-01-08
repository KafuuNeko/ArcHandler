package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.libs.ext.withAlpha

@Composable
fun IconTextItem(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    secondaryText: String? = null,
    onClick: (() -> Unit)? = null
) {
    var itemModifier = modifier
    onClick?.also {
        itemModifier = itemModifier.clickable { it() }
    }

    AppCard(
        modifier = itemModifier
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(38.dp),
                painter = painter,
                contentDescription = text
            )

            Spacer(modifier = Modifier.width(5.dp))

            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                )
                secondaryText?.also {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurface.withAlpha(0.5f)
                    )
                }
            }
        }
    }
}