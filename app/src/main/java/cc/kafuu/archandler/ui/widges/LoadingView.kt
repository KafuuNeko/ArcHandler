package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.LoadState
import cc.kafuu.archandler.ui.theme.MarkColor
import cc.kafuu.archandler.ui.theme.MarkDarkColor

@Composable
fun AppLoadSwitch(
    modifier: Modifier = Modifier,
    loadState: LoadState
) {
    when (loadState) {
        LoadState.None -> Unit

        LoadState.ExternalStoragesLoading,
        LoadState.DirectoryLoading -> GeneralFullLoadingView(modifier)

        is LoadState.Pasting -> GeneralFullLoadingView(
            modifier = modifier,
            isDisplayMark = true,
            message = stringResource(
                if (loadState.isMoving) R.string.moving_message else R.string.copying_message,
                loadState.src.name
            )
        )
    }
}

@Composable
private fun GeneralFullLoadingView(
    modifier: Modifier = Modifier,
    isDisplayMark: Boolean = false,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .run {
                if (!isDisplayMark) return@run this
                background(if (isSystemInDarkTheme()) MarkDarkColor else MarkColor)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        message?.run {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier.padding(horizontal = 30.dp),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}