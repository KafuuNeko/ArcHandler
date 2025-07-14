package cc.kafuu.archandler.feature.main.ui.scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.main.model.MainDrawerMenuEnum
import cc.kafuu.archandler.feature.main.presentation.MainUiIntent

@Composable
fun MainScaffoldDrawer(
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(220.dp),
        drawerState = drawerState
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DrawerHeader()

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            DrawerMenuList(emitIntent = emitIntent)
        }
    }
}

@Composable
private fun DrawerHeader() {
    val appVersionName = LocalContext.current.run {
        packageManager.getPackageInfo(applicationContext.packageName, 0)?.versionName
    } ?: stringResource(R.string.unknown_version)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Image(
            modifier = Modifier.size(64.dp),
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Version: $appVersionName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
        )
    }
}

@Composable
private fun DrawerMenuList(
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column {
        MainDrawerMenuEnum.entries.forEach {
            DrawerMenuOption(
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                emitIntent(MainUiIntent.MainDrawerMenuClick(it))
            }
        }
    }
}

@Composable
private fun DrawerMenuOption(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onMenuClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = icon,
            contentDescription = title
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1
        )
    }
}