package cc.kafuu.archandler.feature.settings.model

import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiIntent
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiState

enum class SettingItem(
    val titleResId: Int,
    val descriptionResId: Int
) {
    SHOW_HIDDEN_FILES(
        R.string.show_hidden_files,
        R.string.show_hidden_files_description
    ),
    SHOW_UNREADABLE_DIRECTORIES(
        R.string.show_unreadable_directories,
        R.string.show_unreadable_directories_description
    ),
    SHOW_UNREADABLE_FILES(
        R.string.show_unreadable_files,
        R.string.show_unreadable_files_description
    );

    fun getCurrentValue(state: SettingsUiState.Normal): Boolean {
        return when (this) {
            SHOW_HIDDEN_FILES -> state.showHiddenFiles
            SHOW_UNREADABLE_DIRECTORIES -> state.showUnreadableDirectories
            SHOW_UNREADABLE_FILES -> state.showUnreadableFiles
        }
    }

    fun getToggleIntent(enabled: Boolean): SettingsUiIntent {
        return when (this) {
            SHOW_HIDDEN_FILES -> SettingsUiIntent.ToggleShowHiddenFiles(enabled)
            SHOW_UNREADABLE_DIRECTORIES -> SettingsUiIntent.ToggleShowUnreadableDirectories(enabled)
            SHOW_UNREADABLE_FILES -> SettingsUiIntent.ToggleShowUnreadableFiles(enabled)
        }
    }
}