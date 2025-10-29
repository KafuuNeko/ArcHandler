package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.core.ActivityPreview

@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String,
    height: Dp = 50.dp,
    backIconPainter: Painter = painterResource(R.drawable.ic_back),
    onBack: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.clickable { onBack() },
            painter = backIconPainter,
            contentDescription = stringResource(R.string.back)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(heightDp = 50)
@Composable
private fun TitleBarDarkPreview() {
    ActivityPreview(darkTheme = true) {
        AppTopBar(title = "Test")
    }
}

@Preview(heightDp = 50)
@Composable
private fun TitleBarLightPreview() {
    ActivityPreview(darkTheme = false) {
        AppTopBar(title = "Test")
    }
}