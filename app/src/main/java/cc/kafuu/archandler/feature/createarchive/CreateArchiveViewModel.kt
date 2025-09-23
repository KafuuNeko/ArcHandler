package cc.kafuu.archandler.feature.createarchive

import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressEncryptable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressLevelConfigurable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressSplittable
import cc.kafuu.archandler.feature.createarchive.extensions.getPackageOptions
import cc.kafuu.archandler.feature.createarchive.extensions.withLevel
import cc.kafuu.archandler.feature.createarchive.extensions.withPassword
import cc.kafuu.archandler.feature.createarchive.extensions.withSplitEnable
import cc.kafuu.archandler.feature.createarchive.extensions.withSplitSize
import cc.kafuu.archandler.feature.createarchive.extensions.withSplitUnit
import cc.kafuu.archandler.feature.createarchive.model.ArchiveFormat
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveLoadState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveViewEvent
import cc.kafuu.archandler.libs.archive.ArchiveManager
import cc.kafuu.archandler.libs.archive.model.CompressionOption
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.extensions.getNameExtension
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.model.CreateArchiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class CreateArchiveViewModel :
    CoreViewModelWithEvent<CreateArchiveUiIntent, CreateArchiveUiState, CreateArchiveViewEvent>(
        initStatus = CreateArchiveUiState.None
    ),
    KoinComponent {
    // 数据传递管理器
    private val mDataTransferManager by inject<DataTransferManager>()

    // 压缩包管理器
    private val mArchiveManager by inject<ArchiveManager>()

    // 最近一次打包压缩包文件
    private var mLastArchiveFile: File? = null

    @UiIntentObserver(CreateArchiveUiIntent.Init::class)
    fun onCreate(event: CreateArchiveUiIntent.Init) {
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
            ArchiveFormat.TarWithBZip2 -> CreateArchiveOptionState.TarWithBZip2()
        }
        uiState.copy(archiveOptions = archiveOptions).setup()
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

    @UiIntentObserver(CreateArchiveUiIntent.CreateArchive::class)
    suspend fun onCreateArchive() = enqueueAsyncTask(Dispatchers.IO) {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return@enqueueAsyncTask
        // 执行打包前检查工作
        if (!uiState.doPrePackageCheck()) return@enqueueAsyncTask
        // 置为打包状态
        uiState.copy(
            loadState = CreateArchiveLoadState.Packing(R.string.preparing_message)
        ).setup()
        // 执行打包流水线
        var sourceFiles = uiState.files
        var targetFileName = uiState.targetFileName
        var preArchiveFile: File? = null
        uiState.getPackageOptions().forEach {
            coroutineContext.ensureActive()
            val archiveFile = it.doPacking(
                sourceFiles = sourceFiles,
                targetDirectory = uiState.targetDirectory,
                targetFileName = targetFileName
            ) ?: run {
                uiState.copy(loadState = CreateArchiveLoadState.None).setup()
                CreateArchiveViewEvent.ToastMessageByResId(R.string.packaging_failed_message).emit()
                return@enqueueAsyncTask
            }
            preArchiveFile?.delete()
            preArchiveFile = archiveFile
            sourceFiles = listOf(archiveFile)
            targetFileName = archiveFile.name
        }
        // 流程结束
        uiState.copy(loadState = CreateArchiveLoadState.None).setup()
        CreateArchiveUiState.Finished.setup()
    }

    private suspend fun CreateArchiveUiState.Normal.doPrePackageCheck(): Boolean {
        // 检查目标文件名是否为空
        if (targetFileName.isEmpty()) {
            CreateArchiveViewEvent.ToastMessageByResId(
                R.string.archive_name_empty_message
            ).emit()
            return false
        }
        // 检查目录是否可写入
        if (!targetDirectory.canWrite()) {
            CreateArchiveViewEvent.ToastMessageByResId(
                R.string.no_directory_write_permission_message
            ).emit()
            return false
        }
        // 检查压缩包文件是否冲突
        val hasFileNameConflict = getPackageOptions().map {
            File(targetDirectory, "${targetFileName}.${it.getNameExtension()}")
        }.any { it.exists() }
        if (hasFileNameConflict) {
            CreateArchiveViewEvent.ToastMessageByResId(
                R.string.archive_name_conflict_message
            ).emit()
            return false
        }
        return true
    }

    private suspend fun CompressionOption.doPacking(
        sourceFiles: List<File>,
        targetDirectory: File,
        targetFileName: String
    ): File? {
        val packageFileName = "$targetFileName.${getNameExtension()}"
        val archiveFile = File(targetDirectory, packageFileName)
        mLastArchiveFile = archiveFile
        val isSuccessful = mArchiveManager.createPacker(
            archiveFile = archiveFile,
            compressionOption = this
        ).pack(sourceFiles) { current, total, filePath ->
            getOrNull<CreateArchiveUiState.Normal>()?.copy(
                loadState = CreateArchiveLoadState.Packing(
                    message = R.string.packing_message,
                    currentFile = File(filePath),
                    progression = current to total
                ),
            )?.setup()
        }
        return if (!isSuccessful && archiveFile.exists()) {
            archiveFile.delete()
            null
        } else {
            archiveFile
        }
    }

    @UiIntentObserver(CreateArchiveUiIntent.CancelPackingJob::class)
    suspend fun onCancelPackingJob() {
        val uiState = getOrNull<CreateArchiveUiState.Normal>() ?: return
        if (uiState.loadState is CreateArchiveLoadState.Packing) {
            cancelActiveTaskAndRestore()
            mLastArchiveFile?.delete()
            uiState.copy(loadState = CreateArchiveLoadState.None).setup()
        }
    }

}