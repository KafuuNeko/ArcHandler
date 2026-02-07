package cc.kafuu.archandler.feature.settings.presentation

import cc.kafuu.archandler.libs.model.AppInfo
import cc.kafuu.archandler.libs.model.DefaultAppType
import cc.kafuu.archandler.libs.model.LayoutType

sealed class SettingsUiState {
    data object None : SettingsUiState()

    data class Normal(
        // 显示设置
        val showHiddenFiles: Boolean = false,
        val showUnreadableDirectories: Boolean = false,
        val showUnreadableFiles: Boolean = true,
        val layoutType: LayoutType = LayoutType.LIST,
        // 默认应用包名以及名称，null 表示"总是询问"
        val defaultAppImages: String? = null,
        val defaultAppImagesDisplayName: String = "",
        val defaultAppVideos: String? = null,
        val defaultAppVideosDisplayName: String = "",
        val defaultAppDocuments: String? = null,
        val defaultAppDocumentsDisplayName: String = "",
        // 对话框状态
        val dialogState: SettingsDialogState = SettingsDialogState.None
    ) : SettingsUiState() {
        fun getDefaultAppDisplayName(type: DefaultAppType): String {
            return when (type) {
                DefaultAppType.IMAGES -> defaultAppImagesDisplayName
                DefaultAppType.VIDEOS -> defaultAppVideosDisplayName
                DefaultAppType.DOCUMENTS -> defaultAppDocumentsDisplayName
            }
        }
    }

    data object Finished : SettingsUiState()
}

/**
 * 设置页面对话框状态
 */
sealed class SettingsDialogState {
    data object None : SettingsDialogState()

    data class AppPicker(
        val type: DefaultAppType,
        val mimeTypes: List<String>,
        val title: String,
        val apps: List<AppInfo>
    ) : SettingsDialogState()
}
