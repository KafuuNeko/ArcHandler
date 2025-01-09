package cc.kafuu.archandler.ui.widges

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cc.kafuu.archandler.libs.model.LoadingState
import cc.kafuu.archandler.ui.theme.MarkColor
import cc.kafuu.archandler.ui.theme.MarkDarkColor

@Composable
fun AppLoadingView(
    modifier: Modifier = Modifier,
    loadingState: LoadingState
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .run {
                if (!loadingState.displayMark) return@run this
                background(if (isSystemInDarkTheme()) MarkDarkColor else MarkColor)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}