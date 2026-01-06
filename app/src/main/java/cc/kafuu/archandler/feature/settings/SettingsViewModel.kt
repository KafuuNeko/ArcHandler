package cc.kafuu.archandler.feature.settings

import cc.kafuu.archandler.feature.settings.presentation.SettingsUiIntent
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiState
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver

class SettingsViewModel : CoreViewModelWithEvent<SettingsUiIntent, SettingsUiState>(
    initStatus = SettingsUiState.None
) {
    @UiIntentObserver(SettingsUiIntent.Init::class)
    private fun onInit() {
        if (!isStateOf<SettingsUiState.None>()) return
        SettingsUiState.Normal(
            showHiddenFiles = AppModel.isShowHiddenFiles,
            showUnreadableDirectories = AppModel.isShowUnreadableDirectories,
            showUnreadableFiles = AppModel.isShowUnreadableFiles
        ).setup()
    }

    @UiIntentObserver(SettingsUiIntent.Back::class)
    private fun onBack() {
        SettingsUiState.Finished.setup()
    }

    @UiIntentObserver(SettingsUiIntent.ToggleShowHiddenFiles::class)
    private suspend fun onToggleShowHiddenFiles(intent: SettingsUiIntent.ToggleShowHiddenFiles) {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        AppModel.isShowHiddenFiles = intent.enabled
        state.copy(showHiddenFiles = intent.enabled).setup()
    }

    @UiIntentObserver(SettingsUiIntent.ToggleShowUnreadableDirectories::class)
    private suspend fun onToggleShowUnreadableDirectories(intent: SettingsUiIntent.ToggleShowUnreadableDirectories) {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        AppModel.isShowUnreadableDirectories = intent.enabled
        state.copy(showUnreadableDirectories = intent.enabled).setup()
    }

    @UiIntentObserver(SettingsUiIntent.ToggleShowUnreadableFiles::class)
    private suspend fun onToggleShowUnreadableFiles(intent: SettingsUiIntent.ToggleShowUnreadableFiles) {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        AppModel.isShowUnreadableFiles = intent.enabled
        state.copy(showUnreadableFiles = intent.enabled).setup()
    }
}
