package cc.kafuu.archandler.feature.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.settings.model.DefaultAppSetting
import cc.kafuu.archandler.feature.settings.model.ToggleSetting
import cc.kafuu.archandler.feature.settings.presentation.SettingsDialogState
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiIntent
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiState
import cc.kafuu.archandler.libs.extensions.withAlpha
import cc.kafuu.archandler.ui.dialogs.AppPickerDialog
import cc.kafuu.archandler.ui.widges.AppTopBar
import cc.kafuu.archandler.ui.widges.SettingItem


@Composable
fun SettingsLayout(
    uiState: SettingsUiState,
    emitIntent: (uiIntent: SettingsUiIntent) -> Unit = {}
) {
    when (uiState) {
        SettingsUiState.None, SettingsUiState.Finished -> Unit

        is SettingsUiState.Normal -> {
            Normal(uiState, emitIntent)
            DialogSwitch(uiState.dialogState, emitIntent)
        }
    }
}

@Composable
private fun DialogSwitch(
    dialogState: SettingsDialogState,
    emitIntent: (uiIntent: SettingsUiIntent) -> Unit = {}
) {
    when (dialogState) {
        SettingsDialogState.None -> Unit

        is SettingsDialogState.AppPicker -> AppPickerDialog(
            title = dialogState.title,
            apps = dialogState.apps,
            onDismissRequest = {
                emitIntent(SettingsUiIntent.DismissAppPicker)
            },
            onAppSelected = { appInfo ->
                emitIntent(SettingsUiIntent.SetDefaultApp(dialogState.type, appInfo.packageName))
                emitIntent(SettingsUiIntent.DismissAppPicker)
            },
            onResetToAsk = {
                emitIntent(SettingsUiIntent.SetDefaultApp(dialogState.type, null))
                emitIntent(SettingsUiIntent.DismissAppPicker)
            }
        )
    }
}

@Composable
private fun Normal(
    uiState: SettingsUiState.Normal,
    emitIntent: (uiIntent: SettingsUiIntent) -> Unit = {}
) {
    BackHandler {
        emitIntent(SettingsUiIntent.Back)
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings),
                onBack = { emitIntent(SettingsUiIntent.Back) }
            )
        }
    ) { paddingValues ->
        SettingsViewContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            emitIntent = emitIntent
        )
    }
}

@Composable
private fun SettingsViewContent(
    uiState: SettingsUiState.Normal,
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: SettingsUiIntent) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.settings_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.withAlpha(0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Toggle settings items
        items(ToggleSetting.entries.toTypedArray()) { item ->
            ToggleSettingItem(
                title = stringResource(item.titleResId),
                description = stringResource(item.descriptionResId),
                checked = item.getCurrentValue(uiState),
                onCheckedChange = { enabled ->
                    emitIntent(item.getToggleIntent(enabled))
                }
            )
        }

        // Default app settings header
        item {
            Text(
                text = stringResource(R.string.default_app_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Default app settings items
        items(DefaultAppSetting.entries.toTypedArray()) { setting ->
            val currentAppName = uiState.getDefaultAppDisplayName(setting.type)

            DefaultAppSettingItem(
                title = stringResource(setting.titleResId),
                currentAppName = currentAppName,
                onClick = {
                    emitIntent(SettingsUiIntent.ShowAppPicker(setting.type))
                }
            )
        }
    }
}

@Composable
private fun ToggleSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.primary,
        checkedTrackColor = MaterialTheme.colorScheme.primary.withAlpha(0.5f),
        checkedBorderColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.2f),
        uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.2f),
        disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.5f),
        disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.2f),
        disabledCheckedBorderColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.2f),
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.5f),
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.2f),
        disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.2f)
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        SettingItem(
            modifier = Modifier,
            title = title,
            description = description
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = switchColors
            )
        }
    }
}

@Composable
private fun DefaultAppSettingItem(
    title: String,
    currentAppName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        SettingItem(
            modifier = Modifier,
            title = title,
            description = currentAppName
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.withAlpha(0.6f)
            )
        }
    }
}
