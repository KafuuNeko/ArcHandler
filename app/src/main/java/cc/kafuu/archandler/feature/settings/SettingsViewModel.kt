package cc.kafuu.archandler.feature.settings

import cc.kafuu.archandler.feature.settings.presentation.SettingsDialogState
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiIntent
import cc.kafuu.archandler.feature.settings.presentation.SettingsUiState
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.model.DefaultAppType
import cc.kafuu.archandler.libs.model.LayoutType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class SettingsViewModel : CoreViewModelWithEvent<SettingsUiIntent, SettingsUiState>(
    initStatus = SettingsUiState.None
), KoinComponent {
    private val mAppLib by inject<AppLibs>()

    @UiIntentObserver(SettingsUiIntent.Init::class)
    private fun onInit() {
        if (!isStateOf<SettingsUiState.None>()) return
        val layoutType = LayoutType.fromValue(AppModel.listLayoutType)
        SettingsUiState.Normal(
            showHiddenFiles = AppModel.isShowHiddenFiles,
            showUnreadableDirectories = AppModel.isShowUnreadableDirectories,
            showUnreadableFiles = AppModel.isShowUnreadableFiles,
            layoutType = layoutType,
            defaultAppImages = AppModel.defaultAppImages,
            defaultAppVideos = AppModel.defaultAppVideos,
            defaultAppDocuments = AppModel.defaultAppDocuments,
            defaultAppImagesDisplayName = mAppLib.getAppDisplayName(AppModel.defaultAppImages),
            defaultAppVideosDisplayName = mAppLib.getAppDisplayName(AppModel.defaultAppVideos),
            defaultAppDocumentsDisplayName = mAppLib.getAppDisplayName(AppModel.defaultAppDocuments)
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

    @UiIntentObserver(SettingsUiIntent.SwitchLayoutType::class)
    private suspend fun onSwitchLayoutType(intent: SettingsUiIntent.SwitchLayoutType) {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        AppModel.listLayoutType = intent.layoutType.value
        state.copy(layoutType = intent.layoutType).setup()
    }

    @UiIntentObserver(SettingsUiIntent.SetDefaultApp::class)
    private suspend fun onSetDefaultApp(intent: SettingsUiIntent.SetDefaultApp) {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        val context = get<android.content.Context>()
        val packageName = intent.packageName
        val displayName = mAppLib.getAppDisplayName(packageName)

        when (intent.type) {
            DefaultAppType.IMAGES -> {
                AppModel.defaultAppImages = packageName
                state.copy(
                    defaultAppImages = packageName,
                    defaultAppImagesDisplayName = displayName
                ).setup()
            }

            DefaultAppType.VIDEOS -> {
                AppModel.defaultAppVideos = packageName
                state.copy(
                    defaultAppVideos = packageName,
                    defaultAppVideosDisplayName = displayName
                ).setup()
            }

            DefaultAppType.DOCUMENTS -> {
                AppModel.defaultAppDocuments = packageName
                state.copy(
                    defaultAppDocuments = packageName,
                    defaultAppDocumentsDisplayName = displayName
                ).setup()
            }
        }
    }

    @UiIntentObserver(SettingsUiIntent.ShowAppPicker::class)
    private fun onShowAppPicker(intent: SettingsUiIntent.ShowAppPicker) {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        val mimeTypes = intent.type.mimeTypes
        val title = state.getDefaultAppDisplayName(intent.type)

        val dialogState = SettingsDialogState.AppPicker(
            type = intent.type,
            mimeTypes = mimeTypes,
            title = title,
            apps = mAppLib.getAppsForMimeType(mimeTypes)
        )
        state.copy(dialogState = dialogState).setup()
    }

    @UiIntentObserver(SettingsUiIntent.DismissAppPicker::class)
    private fun onDismissAppPicker() {
        val state = getOrNull<SettingsUiState.Normal>() ?: return
        state.copy(dialogState = SettingsDialogState.None).setup()
    }

}
