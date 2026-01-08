package cc.kafuu.archandler.feature.settings.presentation

sealed class SettingsUiIntent {
    data object Init : SettingsUiIntent()
    data object Back : SettingsUiIntent()
    data class ToggleShowHiddenFiles(val enabled: Boolean) : SettingsUiIntent()
    data class ToggleShowUnreadableDirectories(val enabled: Boolean) : SettingsUiIntent()
    data class ToggleShowUnreadableFiles(val enabled: Boolean) : SettingsUiIntent()
    data class SwitchLayoutType(val layoutType: cc.kafuu.archandler.libs.model.LayoutType) : SettingsUiIntent()
}
