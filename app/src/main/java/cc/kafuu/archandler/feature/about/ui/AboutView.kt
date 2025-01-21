package cc.kafuu.archandler.feature.about.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.ui.widges.AppTitleBar

@Composable
fun AboutViewBody(
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTitleBar(
                title = stringResource(R.string.about),
                onBack = onBack
            )
        },
    ) { paddingValues ->
        AboutViewContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun AboutViewContent(
    modifier: Modifier = Modifier
) {
    val appVersionName = LocalContext.current.run {
        packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
    } ?: stringResource(R.string.unknown_version)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(100.dp),
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = stringResource(R.string.app_name)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = appVersionName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}