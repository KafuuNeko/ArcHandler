package cc.kafuu.archandler.feature.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent
import cc.kafuu.archandler.libs.core.ActivityPreview
import cc.kafuu.archandler.ui.widges.AppPrimaryButton

@Composable
fun PermissionDeniedView(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(64.dp),
            painter = painterResource(R.drawable.ic_folder_off),
            contentDescription = stringResource(R.string.not_permission_in_main_view)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.not_permission_in_main_view),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                emitIntent(MainUiIntent.JumpFilePermissionSetting)
            },
            text = stringResource(R.string.enable_permission),
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun PermissionDeniedBodyPreview() {
    ActivityPreview(darkTheme = true) {
        PermissionDeniedView()
    }
}