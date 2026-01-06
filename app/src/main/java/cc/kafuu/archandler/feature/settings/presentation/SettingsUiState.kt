package cc.kafuu.archandler.feature.settings.presentation

sealed class SettingsUiState {
    data object None : SettingsUiState()

    data class Normal(
        val showHiddenFiles: Boolean = false,
        val showUnreadableDirectories: Boolean = false,
        val showUnreadableFiles: Boolean = true
    ) : SettingsUiState()

    data object Finished : SettingsUiState()
}
