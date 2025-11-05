package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File
import java.nio.file.Path

@Composable
fun DirectoryPathBar(
    storageData: StorageData,
    directoryPath: Path,
    modifier: Modifier = Modifier,
    onClickDevice: ()-> Unit,
    onClick: (File) -> Unit
) {
    val baseURI = storageData.directory.toURI()
    val targetURI = File(directoryPath.toString()).toURI()
    val segments = baseURI.relativize(targetURI).path
        .takeIf { it.isNotEmpty() }
        ?.trim('/')?.split('/') ?: emptyList()

    val scrollState = rememberScrollState()

    LaunchedEffect(segments, directoryPath) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .clickable { onClickDevice() },
            text = stringResource(R.string.storage_volume),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )

        Image(
            modifier = Modifier
                .padding(horizontal = 2.5.dp)
                .size(12.dp),
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .clickable { onClick(storageData.directory) },
            text = storageData.name,
            style = MaterialTheme.typography.headlineMedium,
            color = if (segments.isEmpty()) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            }
        )

        var parentFile = storageData.directory
        segments.forEachIndexed { index, segment ->
            val currentFile = File(parentFile, segment)

            Image(
                modifier = Modifier
                    .padding(horizontal = 2.5.dp)
                    .size(12.dp),
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .clickable { onClick(currentFile) },
                text = segment,
                style = MaterialTheme.typography.headlineMedium,
                color = if (segments.size == index + 1) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                }
            )

            parentFile = currentFile
        }
    }
}