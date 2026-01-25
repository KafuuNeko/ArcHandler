package cc.kafuu.archandler.feature.archiveview

import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewDialogState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewLoadState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiIntent
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewUiState
import cc.kafuu.archandler.feature.archiveview.presentation.ArchiveViewViewEvent
import cc.kafuu.archandler.feature.storagepicker.StoragePickerActivity
import cc.kafuu.archandler.feature.storagepicker.model.PickMode
import cc.kafuu.archandler.feature.storagepicker.model.StoragePickerParams
import cc.kafuu.archandler.feature.storagepicker.model.StoragePickerResult
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.archive.ArchiveManager
import cc.kafuu.archandler.libs.archive.IArchive
import cc.kafuu.archandler.libs.archive.IPasswordProvider
import cc.kafuu.archandler.libs.archive.model.ArchiveEntry
import cc.kafuu.archandler.libs.core.AppViewEvent
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.manager.CacheManager
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.AppCacheType
import cc.kafuu.archandler.libs.model.ArchiveViewData
import cc.kafuu.archandler.libs.model.LayoutType
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.libs.utils.DeferredResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import java.util.Stack

class ArchiveViewViewModel : CoreViewModelWithEvent<ArchiveViewUiIntent, ArchiveViewUiState>(
    initStatus = ArchiveViewUiState.None
), KoinComponent {
    // 压缩包管理器
    private val mArchiveManager by inject<ArchiveManager>()

    // 应用缓存管理器
    private val mCacheManager by inject<CacheManager>()

    // 文件管理器
    private val mFileManager by inject<FileManager>()

    // 数据传递管理器
    private val mDataTransferManager by inject<DataTransferManager>()

    // 当前打开的压缩包实例
    private var mArchive: IArchive? = null

    // 缓存的压缩包完整文件列表
    private var mCachedAllEntries: List<ArchiveEntry>? = null

    // 目录路径栈，用于返回导航
    private val mPathStack = Stack<String>()

    // 压缩包密码提供请求接口
    val mPasswordProvider = object : IPasswordProvider {
        override suspend fun getPassword(file: File): String? {
            val deferredResult = DeferredResult<String?>()
            awaitStateOf<ArchiveViewUiState.Normal>().copy(
                dialogState = ArchiveViewDialogState.PasswordInput(
                    file = file,
                    deferredResult = deferredResult
                )
            ).setup()
            return deferredResult.awaitCompleted()
        }
    }

    /**
     * 初始化
     */
    @UiIntentObserver(ArchiveViewUiIntent.Init::class)
    private suspend fun onInit(intent: ArchiveViewUiIntent.Init) {
        if (!isStateOf<ArchiveViewUiState.None>()) return
        val initData = intent.transferId?.let {
            mDataTransferManager.takeAs<ArchiveViewData>(intent.transferId)
        } ?: run {
            ArchiveViewUiState.Finished.setup()
            return
        }
        loadArchive(initData.archiveFile)
    }

    /**
     * 加载压缩包
     */
    private suspend fun loadArchive(archiveFile: File) {
        // 从AppModel读取布局类型
        val layoutType = LayoutType.fromValue(AppModel.listLayoutType)
        ArchiveViewUiState.Normal(
            archiveFile = archiveFile,
            loadState = ArchiveViewLoadState.ArchiveOpening,
            layoutType = layoutType
        ).setup()
        val archive = runCatching {
            withContext(Dispatchers.IO) {
                mArchiveManager.openArchive(archiveFile)
            }
        }.getOrNull() ?: run {
            errorMessage(null)
            ArchiveViewUiState.Finished.setup()
            return
        }
        val opened = runCatching {
            archive.open(mPasswordProvider)
        }.getOrNull() ?: false
        if (!opened) {
            // 清除对话框状态
            getOrNull<ArchiveViewUiState.Normal>()?.copy(
                dialogState = ArchiveViewDialogState.None
            )?.setup()
            errorMessage(null)
            ArchiveViewUiState.Finished.setup()
            return
        }
        mArchive = archive
        getOrNull<ArchiveViewUiState.Normal>()?.copy(
            loadState = ArchiveViewLoadState.LoadingEntries,
            dialogState = ArchiveViewDialogState.None
        )?.setup()

        // 缓存完整的文件列表
        mCachedAllEntries = runCatching {
            archive.list("")
        }.getOrNull() ?: emptyList()

        loadEntries("")
    }

    /**
     * 加载条目列表
     */
    private fun loadEntries(currentPath: String) {
        val allEntries = mCachedAllEntries ?: return

        // 过滤出当前路径下的直接子项
        val normalizedPath = if (currentPath.isEmpty()) {
            ""
        } else {
            currentPath.trimEnd('/') + "/"
        }

        val currentEntries = if (currentPath.isEmpty()) {
            // 根目录：显示所有顶级项
            allEntries.filter { entry ->
                val pathParts = entry.path.split('/').filter { it.isNotEmpty() }
                pathParts.size == 1
            }
        } else {
            // 子目录：显示指定路径下的直接子项
            val pathPrefix = normalizedPath
            val seenPaths = mutableSetOf<String>()

            allEntries.filter { entry ->
                val entryPath = entry.path.trimEnd('/') + "/"
                entryPath.startsWith(pathPrefix) && entry.path != currentPath.trimEnd('/')
            }.mapNotNull { entry ->
                val relativePath = entry.path.removePrefix(pathPrefix)
                val pathParts = relativePath.split('/').filter { it.isNotEmpty() }
                if (pathParts.isEmpty()) return@mapNotNull null

                val firstPart = pathParts[0]
                val childPath = if (currentPath.isEmpty()) firstPart else "$currentPath/$firstPart"

                // 避免重复
                if (seenPaths.contains(childPath)) return@mapNotNull null
                seenPaths.add(childPath)

                // 创建一个新的条目，代表直接子项
                entry.copy(
                    path = childPath,
                    name = firstPart
                )
            }
        }
        val state = getOrNull<ArchiveViewUiState.Normal>()
            ?: ArchiveViewUiState.Normal(archiveFile = File(""))
        state.copy(
            currentPath = currentPath,
            entries = currentEntries.sortedWith(compareBy<ArchiveEntry> { !it.isDirectory }.thenBy { it.name }),
            loadState = ArchiveViewLoadState.None
        ).setup()
    }

    /**
     * 返回操作
     */
    @UiIntentObserver(ArchiveViewUiIntent.Back::class)
    private fun onBack() {
        val uiState = getOrNull<ArchiveViewUiState.Normal>() ?: run {
            ArchiveViewUiState.Finished.setup()
            return
        }
        if (uiState.currentPath.isEmpty()) {
            // 在根目录，返回上一页
            ArchiveViewUiState.Finished.setup()
        } else {
            // 返回上一级目录
            val parentPath = if (mPathStack.isNotEmpty()) {
                // 如果路径栈不为空，使用栈中弹出的路径
                mPathStack.pop()
            } else {
                // 如果路径栈为空，通过计算父路径返回
                uiState.currentPath.split('/').dropLast(1).joinToString("/")
            }
            loadEntries(parentPath)
        }
    }

    /**
     * 关闭页面
     */
    @UiIntentObserver(ArchiveViewUiIntent.Close::class)
    private fun onClose() {
        ArchiveViewUiState.Finished.setup()
    }

    /**
     * 条目选择
     */
    @UiIntentObserver(ArchiveViewUiIntent.EntrySelected::class)
    private fun onEntrySelected(intent: ArchiveViewUiIntent.EntrySelected) {
        val entry = intent.entry
        if (!entry.isDirectory) return
        val newPath = if (getOrNull<ArchiveViewUiState.Normal>()?.currentPath?.isEmpty() == true) {
            entry.path
        } else {
            "${getOrNull<ArchiveViewUiState.Normal>()?.currentPath}/${entry.name}"
        }
        mPathStack.push(getOrNull<ArchiveViewUiState.Normal>()?.currentPath ?: "")
        loadEntries(newPath)
    }

    /**
     * 路径导航
     */
    @UiIntentObserver(ArchiveViewUiIntent.PathSelected::class)
    private fun onPathSelected(intent: ArchiveViewUiIntent.PathSelected) {
        val targetPath = intent.path
        val currentPath = getOrNull<ArchiveViewUiState.Normal>()?.currentPath ?: ""
        if (targetPath == currentPath) return
        loadEntries(targetPath)
        mPathStack.clear()
    }

    /**
     * 解压压缩包
     */
    @UiIntentObserver(ArchiveViewUiIntent.ExtractArchive::class)
    private suspend fun onExtractArchive() {
        val uiState = getOrNull<ArchiveViewUiState.Normal>() ?: return
        val archiveFile = uiState.archiveFile

        // 获取压缩包文件所在的存储设备
        val storageData = getStorageDataForFile(archiveFile)

        val params = StoragePickerParams(
            pickMode = PickMode.ChooseDirectory,
            defaultStorage = storageData,
            defaultPath = archiveFile.parent
        )
        ArchiveViewViewEvent.SelectExtractDirectory(
            StoragePickerActivity.params(mDataTransferManager, params)
        ).emit()
    }

    /**
     * 获取文件所在的存储设备
     */
    private fun getStorageDataForFile(file: File): StorageData? {
        val storageVolumes = mFileManager.getMountedStorageVolumes()
        val filePath = file.absolutePath
        return storageVolumes.firstOrNull { storage ->
            val storagePath = storage.directory.absolutePath
            filePath.startsWith(storagePath)
        }
    }

    /**
     * 解压到目录完成
     */
    @UiIntentObserver(ArchiveViewUiIntent.ExtractToDirectoryCompleted::class)
    private suspend fun onExtractToDirectoryCompleted(intent: ArchiveViewUiIntent.ExtractToDirectoryCompleted) {
        val uiState = getOrNull<ArchiveViewUiState.Normal>() ?: return
        val result = intent.transferId?.let {
            mDataTransferManager.takeAs<StoragePickerResult.ChooseDirectory>(intent.transferId)
        } ?: return
        val currentState = getOrNull<ArchiveViewUiState.Normal>() ?: return
        currentState.copy(loadState = ArchiveViewLoadState.Extracting(0, "", 0)).setup()
        val archive = mArchive ?: run {
            errorMessage(null)
            currentState.copy(loadState = ArchiveViewLoadState.None).setup()
            return
        }
        val destDir = File(result.directoryPath.toString())
        val resultExtract = runCatching {
            withContext(Dispatchers.IO) {
                archive.extractAll(destDir) { index, path, target ->
                    withContext(Dispatchers.Main) {
                        uiState
                            .copy(loadState = ArchiveViewLoadState.Extracting(index, path, target))
                            .setup()
                    }
                    currentCoroutineContext().ensureActive()
                }
            }
        }
        uiState.setup()
        mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
        if (resultExtract.isFailure) {
            errorMessage(resultExtract.exceptionOrNull())
        } else {
            AppViewEvent.PopupToastMessageByResId(R.string.archive_unpacking_success_message).emit()
        }
    }

    /**
     * 取消解压
     */
    @UiIntentObserver(ArchiveViewUiIntent.CancelExtracting::class)
    private suspend fun onCancelExtracting() {
        val uiState = getOrNull<ArchiveViewUiState.Normal>() ?: return
        if (uiState.loadState is ArchiveViewLoadState.Extracting) {
            cancelActiveTaskAndRestore()
            mCacheManager.clearCache(AppCacheType.MERGE_SPLIT_ARCHIVE)
            uiState.copy(loadState = ArchiveViewLoadState.None).setup()
        }
    }

    /**
     * 显示错误消息
     */
    private suspend fun errorMessage(exception: Throwable?) {
        val message = exception?.message ?: get<AppLibs>().getString(R.string.unknown_error)
        AppViewEvent.PopupToastMessage(message).emit()
    }

    /**
     * 切换布局类型
     */
    @UiIntentObserver(ArchiveViewUiIntent.SwitchLayoutType::class)
    fun onSwitchLayoutType(intent: ArchiveViewUiIntent.SwitchLayoutType) {
        val uiState = getOrNull<ArchiveViewUiState.Normal>() ?: return
        AppModel.listLayoutType = intent.layoutType.value
        uiState.copy(layoutType = intent.layoutType).setup()
    }


    override fun onCleared() {
        super.onCleared()
        mArchive?.close()
    }
}

