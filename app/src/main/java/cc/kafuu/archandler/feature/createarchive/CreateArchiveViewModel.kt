package cc.kafuu.archandler.feature.createarchive

import cc.kafuu.archandler.feature.createarchive.capabilities.CompressEncryptable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressLevelConfigurable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressSplittable
import cc.kafuu.archandler.feature.createarchive.extensions.withLevel
import cc.kafuu.archandler.feature.createarchive.extensions.withPassword
import cc.kafuu.archandler.feature.createarchive.extensions.withSplitEnable
import cc.kafuu.archandler.feature.createarchive.extensions.withSplitSize
import cc.kafuu.archandler.feature.createarchive.extensions.withSplitUnit
import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.libs.core.CoreViewModel
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.model.CreateArchiveData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateArchiveViewModel : CoreViewModel<CreateArchiveUiIntent, CreateArchiveUiState>(
    initStatus = CreateArchiveUiState.None
), KoinComponent {
    private val mDataTransferManager by inject<DataTransferManager>()

    @UiIntentObserver(CreateArchiveUiIntent.OnCreate::class)
    fun onCreate(event: CreateArchiveUiIntent.OnCreate) {
        if (!isStateOf<CreateArchiveUiState.None>()) return
        val initData = event.transferId?.let {
            mDataTransferManager.takeAs<CreateArchiveData>(event.transferId)
        } ?: run {
            CreateArchiveUiState.Finished.setup()
            return
        }
        CreateArchiveUiState.Normal(
            files = initData.files,
            targetDirectory = initData.targetDirectory
        ).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.Back::class)
    fun onBack() {
        CreateArchiveUiState.Finished.setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.ArchiveFormatChange::class)
    fun onArchiveFormatChange(event: CreateArchiveUiIntent.ArchiveFormatChange) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.archiveOptions.format == event.format) return
        val archiveOptions = when (event.format) {
            ArchiveFormat.Zip -> CreateArchiveOptionState.Zip()
            ArchiveFormat.SevenZip -> CreateArchiveOptionState.SevenZip()
            ArchiveFormat.Tar -> CreateArchiveOptionState.Tar()
            ArchiveFormat.TarWithGZip -> CreateArchiveOptionState.TarWithGZip()
            ArchiveFormat.BZip2 -> CreateArchiveOptionState.BZip2()
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.PickSavePath::class)
    fun onPickSavePath() {
        // TODO: 打开保存路径选择器
    }

    @UiIntentObserver(CreateArchiveUiIntent.TargetFileNameChange::class)
    fun onTargetFileNameChange(event: CreateArchiveUiIntent.TargetFileNameChange) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        uiState.copy(targetFileName = event.name).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.CompressLevelChange::class)
    fun onCompressLevelChange(event: CreateArchiveUiIntent.CompressLevelChange) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.archiveOptions !is CompressLevelConfigurable) return
        val archiveOptions = uiState.archiveOptions.run {
            withLevel(level = event.level)
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.ArchivePasswordChange::class)
    fun onArchivePasswordChange(event: CreateArchiveUiIntent.ArchivePasswordChange) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.archiveOptions !is CompressEncryptable) return
        val archiveOptions = uiState.archiveOptions.run {
            withPassword(password = event.password)
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.SplitEnabledToggle::class)
    fun onSplitEnabledToggle(event: CreateArchiveUiIntent.SplitEnabledToggle) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.archiveOptions !is CompressSplittable) return
        val archiveOptions = uiState.archiveOptions.run {
            withSplitEnable(event.isEnable)
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.SplitUnitChange::class)
    fun onSplitUnitChange(event: CreateArchiveUiIntent.SplitUnitChange) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.archiveOptions !is CompressSplittable) return
        val archiveOptions = uiState.archiveOptions.run {
            withSplitUnit(event.unit)
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
    }

    @UiIntentObserver(CreateArchiveUiIntent.SplitSizeChange::class)
    fun onSplitSizeChange(event: CreateArchiveUiIntent.SplitSizeChange) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.archiveOptions !is CompressSplittable) return
        val archiveOptions = uiState.archiveOptions.run {
            withSplitSize(event.size)
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
    }
}