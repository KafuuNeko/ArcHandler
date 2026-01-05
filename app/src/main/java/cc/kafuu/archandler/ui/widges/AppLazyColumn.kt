package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun <T> AppLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    userScrollEnabled: Boolean = true,
    emptyState: @Composable (() -> Unit)? = null,
    enableScrollbar: Boolean = true,
    scrollbarSettings: ScrollbarSettings = ScrollbarSettings.Default,
    content: @Composable() (LazyItemScope.(T) -> Unit),
) {
    if (items.isEmpty()) {
        emptyState?.invoke()
    } else {
        if (enableScrollbar) {
            LazyColumnScrollbar(
                state = state,
                settings = scrollbarSettings
            ) {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = verticalArrangement,
                    horizontalAlignment = horizontalAlignment,
                    state = state,
                    contentPadding = contentPadding,
                    reverseLayout = reverseLayout,
                    userScrollEnabled = userScrollEnabled
                ) {
                    items(items, itemContent = content)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                state = state,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                userScrollEnabled = userScrollEnabled
            ) {
                items(items, itemContent = content)
            }
        }
    }
}
