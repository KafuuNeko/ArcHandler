package cc.kafuu.archandler.feature.settings.presentation

import cc.kafuu.archandler.libs.model.DefaultAppType
import cc.kafuu.archandler.libs.model.LayoutType

sealed class SettingsUiIntent {
    data object Init : SettingsUiIntent()
    data object Back : SettingsUiIntent()
    data class ToggleShowHiddenFiles(val enabled: Boolean) : SettingsUiIntent()
    data class ToggleShowUnreadableDirectories(val enabled: Boolean) : SettingsUiIntent()
    data class ToggleShowUnreadableFiles(val enabled: Boolean) : SettingsUiIntent()
    data class SwitchLayoutType(val layoutType: LayoutType) : SettingsUiIntent()

    /**
     * 选择某个文件类型的默认应用
     * @param type 文件类型
     * @param packageName 应用包名，null 表示"总是询问"
     */
    data class SetDefaultApp(val type: DefaultAppType, val packageName: String?) :
        SettingsUiIntent()

    /**
     * 显示应用选择器
     */
    data class ShowAppPicker(val type: DefaultAppType) : SettingsUiIntent()

    /**
     * 关闭应用选择器
     */
    data object DismissAppPicker : SettingsUiIntent()
}
