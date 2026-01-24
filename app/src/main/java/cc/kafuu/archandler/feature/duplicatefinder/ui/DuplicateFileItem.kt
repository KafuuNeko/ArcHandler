package cc.kafuu.archandler.feature.duplicatefinder.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.libs.extensions.getFileType
import cc.kafuu.archandler.libs.extensions.getReadableSize
import cc.kafuu.archandler.libs.model.FileType
import cc.kafuu.archandler.ui.utils.rememberVideoThumbnailPainter
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun DuplicateFileItem(
    modifier: Modifier = Modifier,
    file: File,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onSelectionChange: (File, Boolean) -> Unit = { _, _ -> }
) {
    val painter = file.getFileType().let { type ->
        val defaultIcon = painterResource(type.icon)
        when (type) {
            FileType.Image -> rememberAsyncImagePainter(model = file, placeholder = defaultIcon)
            FileType.Movie -> rememberVideoThumbnailPainter(model = file, placeholder = defaultIcon)
            else -> defaultIcon
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onSelectionChange(file, !selected)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件图标
            Image(
                modifier = Modifier.size(48.dp),
                painter = painter,
                contentDescription = file.name,
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 文件信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = file.parent ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = file.getReadableSize(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 选择框
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(
                checked = selected,
                onCheckedChange = { onSelectionChange(file, it) }
            )
        }
    }
}
