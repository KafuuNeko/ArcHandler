package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.extensions.withAlpha

@Composable
fun AppItemCard(
    modifier: Modifier = Modifier,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    Card(
        modifier = modifier,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.withAlpha(0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.5f),
        ),
        content = content
    )
}

@Composable
fun AppIconTextItemCard(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    secondaryText: String? = null,
    onClick: (() -> Unit)? = null
) {
    var itemModifier = modifier.clip(CardDefaults.shape)
    onClick?.also {
        itemModifier = itemModifier.clickable { it() }
    }

    AppItemCard(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppOptionalIconTextItemCard(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    contentScale: ContentScale = ContentScale.Fit,
    textMaxLine: Int = 1,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    secondaryText: String? = null,
    secondaryTextMaxLine: Int = 1,
    displaySelectBox: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    AppItemCard(
        modifier = modifier
            .clip(CardDefaults.shape)
            .combinedClickable(
                onLongClick = {
                    onLongClick?.invoke()
                },
                onClick = {
                    onClick?.invoke()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(38.dp),
                contentScale = contentScale,
                painter = painter,
                contentDescription = text
            )

            Spacer(modifier = Modifier.width(5.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                    maxLines = textMaxLine,
                    overflow = TextOverflow.Ellipsis
                )
                secondaryText?.also {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurface.withAlpha(0.5f),
                        maxLines = secondaryTextMaxLine,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (displaySelectBox) {
                Spacer(modifier = Modifier.width(5.dp))
                Image(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(if (checked) R.drawable.ic_check_circle else R.drawable.ic_circle),
                    contentDescription = null
                )
            }
        }
    }
}

/**
 * 网格布局的文件项卡片
 *
 * @param modifier 修饰符
 * @param icon 图标
 * @param text 文件名
 * @param selected 是否选中
 * @param showCheckbox 是否显示复选框
 * @param onClick 点击事件
 * @param onLongClick 长按事件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridFileItemCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    text: String,
    secondaryText: String? = null,
    selected: Boolean = false,
    showCheckbox: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.withAlpha(0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标区域
                Box(
                    modifier = Modifier
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 文件名区域
                Text(
                    modifier = Modifier,
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 复选框（多选模式）
            if (showCheckbox) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .zIndex(1f)
                ) {
                    Image(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = onClick),
                        painter = painterResource(if (selected) R.drawable.ic_check_circle else R.drawable.ic_circle),
                        contentDescription = null
                    )
                }
            }
        }
    }
}
