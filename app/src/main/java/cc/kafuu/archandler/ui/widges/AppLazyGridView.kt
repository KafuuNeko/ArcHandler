package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.extensions.withAlpha
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

/**
 * 通用的网格列表视图组件
 * 用于在 Main、StoragePicker、ArchiveView 中显示文件列表
 *
 * @param modifier 修饰符
 * @param items 列表数据项
 * @param contentPadding 内容内边距
 * @param emptyView 空状态视图
 * @param itemContent 列表项内容渲染（列表布局）
 * @param gridItemContent 网格项内容渲染（网格布局）
 */
@Composable
fun <T> AppLazyGridView(
    modifier: Modifier = Modifier,
    items: List<T>,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enableScrollbar: Boolean = true,
    scrollbarSettings: ScrollbarSettings = ScrollbarSettings.Default,
    emptyView: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit = {},
    gridItemContent: @Composable (T) -> Unit = {}
) {
    if (items.isEmpty() && emptyView != null) {
        Box(modifier = modifier) {
            emptyView()
        }
        return
    }
    if (enableScrollbar) {
        LazyVerticalGridScrollbar(
            modifier = modifier,
            state = state,
            settings = scrollbarSettings
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = state,
                columns = GridCells.Fixed(4),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    gridItemContent(item)
                }
            }
        }
    } else {
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Fixed(4),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                gridItemContent(item)
            }
        }
    }
}
