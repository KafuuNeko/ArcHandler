package cc.kafuu.archandler.feature.settings.presentation

import cc.kafuu.archandler.libs.model.LayoutType

sealed class SettingsUiState {
    data object None : SettingsUiState()

    data class Normal(
        val showHiddenFiles: Boolean = false,
        val showUnreadableDirectories: Boolean = false,
        val showUnreadableFiles: Boolean = true,
        val layoutType: LayoutType = LayoutType.LIST
    ) : SettingsUiState()

    data object Finished : SettingsUiState()
}
