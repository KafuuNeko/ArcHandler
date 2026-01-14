package cc.kafuu.archandler.feature.duplicatefinder

import android.content.Context
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderDialogState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderLoadState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderSearchState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderUiIntent
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderUiState
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFinderViewEvent
import cc.kafuu.archandler.feature.duplicatefinder.presentation.DuplicateFileGroup
import cc.kafuu.archandler.libs.core.AppViewEvent
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.extensions.deletes
import cc.kafuu.archandler.libs.extensions.listFilteredFiles
import cc.kafuu.archandler.libs.extensions.sha256Of
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File

class DuplicateFinderViewModel(
    private val targetDirectory: File
) : CoreViewModelWithEvent<DuplicateFinderUiIntent, DuplicateFinderUiState>(
    initStatus = DuplicateFinderUiState.None
), KoinComponent {

    // 收集到的所有文件
    private var mAllFiles: List<File> = emptyList()

    /**
     * 展示错误消息
     */
    private suspend fun errorMessage(exception: Throwable?) {
        val message = exception?.message ?: get<Context>().getString(R.string.unknown_error)
        AppViewEvent.PopupToastMessage(message).emit()
    }

    /**
     * 弹窗通用流程
     */
    private suspend fun <R> DuplicateFinderDialogState.popupAwaitDialogResult(
        onAwaitResult: suspend () -> R
    ): R? {
        val state = awaitStateOf<DuplicateFinderUiState.Normal>() {
            it.dialogState is DuplicateFinderDialogState.None
        }
        val result = state.run {
            copy(dialogState = this@popupAwaitDialogResult).setup()
            onAwaitResult()
        }
        state.setup()
        return result
    }

    /**
     * 页面初始化
     */
    @UiIntentObserver(DuplicateFinderUiIntent.Init::class)
    private suspend fun onInit() {
        if (!isStateOf<DuplicateFinderUiState.None>()) return

        if (!targetDirectory.exists() || !targetDirectory.isDirectory) {
            AppViewEvent.PopupToastMessageByResId(R.string.invalid_directory).emit()
            DuplicateFinderUiState.Finished.setup()
            return
        }

        DuplicateFinderUiState.Normal().setup()
        // 自动开始搜索
        onStartSearch()
    }

    /**
     * 页面返回
     */
    @UiIntentObserver(DuplicateFinderUiIntent.Back::class)
    private suspend fun onBack() {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return

        when {
            state.selectionMode -> {
                // 退出选择模式
                state.copy(selectionMode = false, selectedFiles = emptySet()).setup()
            }
            state.searchState is DuplicateFinderSearchState.Searching ||
            state.loadState !is DuplicateFinderLoadState.None -> Unit
            else -> {
                DuplicateFinderUiState.Finished.setup()
            }
        }
    }

    /**
     * 开始搜索重复文件
     */
    @UiIntentObserver(DuplicateFinderUiIntent.StartSearch::class)
    private suspend fun onStartSearch() = enqueueAsyncTask(Dispatchers.IO) {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return@enqueueAsyncTask
        if (state.searchState !is DuplicateFinderSearchState.Idle) return@enqueueAsyncTask

        try {
            // 收集所有文件
            state.copy(
                searchState = DuplicateFinderSearchState.Searching,
                loadState = DuplicateFinderLoadState.Scanning()
            ).setup()

            mAllFiles = collectAllFiles(targetDirectory)
            currentCoroutineContext().ensureActive()

            // 按文件大小分组
            val sizeGroups = mAllFiles.groupBy { it.length() }.filterValues { it.size > 1 }

            val totalFilesToHash = sizeGroups.values.sumOf { it.size }
            var processedFiles = 0
            val hashGroups = HashMap<String, MutableList<File>>()
            var wastedSpace = 0L

            // 对相同大小的文件计算哈希
            for ((size, files) in sizeGroups) {
                state.copy(
                    loadState = DuplicateFinderLoadState.Hashing(
                        currentFile = files.first(),
                        processedCount = processedFiles,
                        totalCount = totalFilesToHash
                    )
                ).setup()

                val fileHashMap = HashMap<String, File>()
                for (file in files) {
                    currentCoroutineContext().ensureActive()

                    state.copy(
                        loadState = DuplicateFinderLoadState.Hashing(
                            currentFile = file,
                            processedCount = processedFiles,
                            totalCount = totalFilesToHash
                        )
                    ).setup()

                    try {
                        val hash = file.sha256Of()
                        if (fileHashMap.containsKey(hash)) {
                            // 找到重复文件
                            hashGroups.getOrPut(hash) { mutableListOf(fileHashMap[hash]!!) }.add(file)
                            wastedSpace += size
                        } else {
                            fileHashMap[hash] = file
                        }
                    } catch (e: Exception) {
                        // 跳过无法读取的文件
                        e.printStackTrace()
                    }
                    processedFiles++
                }
            }

            currentCoroutineContext().ensureActive()

            // 构建结果
            val duplicateGroups = hashGroups.map { (hash, files) ->
                DuplicateFileGroup(
                    hash = hash,
                    fileSize = files.first().length(),
                    files = files.sortedBy { it.absolutePath }
                )
            }.sortedByDescending { it.fileSize * it.files.size }

            val duplicateFileCount = duplicateGroups.sumOf { it.files.size }

            state.copy(
                searchState = DuplicateFinderSearchState.Success(
                    duplicateGroups = duplicateGroups,
                    totalFiles = mAllFiles.size,
                    duplicateFileCount = duplicateFileCount,
                    wastedSpace = wastedSpace
                ),
                loadState = DuplicateFinderLoadState.None
            ).setup()

        } catch (e: Exception) {
            e.printStackTrace()
            state.copy(
                searchState = DuplicateFinderSearchState.Error(e.message ?: "Unknown error"),
                loadState = DuplicateFinderLoadState.None
            ).setup()
        }
    }

    /**
     * 取消搜索
     */
    @UiIntentObserver(DuplicateFinderUiIntent.CancelSearch::class)
    private suspend fun onCancelSearch() {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return
        if (state.searchState is DuplicateFinderSearchState.Searching) {
            cancelActiveTaskAndRestore()
        }
        // 取消后返回上一页面
        DuplicateFinderUiState.Finished.setup()
    }

    /**
     * 切换选择模式
     */
    @UiIntentObserver(DuplicateFinderUiIntent.ToggleSelection::class)
    private suspend fun onToggleSelection() {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return
        val newMode = !state.selectionMode
        state.copy(
            selectionMode = newMode,
            selectedFiles = if (newMode) emptySet() else state.selectedFiles
        ).setup()
    }

    /**
     * 文件选择
     */
    @UiIntentObserver(DuplicateFinderUiIntent.FileSelect::class)
    private suspend fun onFileSelect(intent: DuplicateFinderUiIntent.FileSelect) {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return
        if (!state.selectionMode) return

        val newSelected = state.selectedFiles.toMutableSet().apply {
            if (intent.selected) {
                add(intent.file)
            } else {
                remove(intent.file)
            }
        }

        state.copy(selectedFiles = newSelected).setup()
    }

    /**
     * 选中组内所有文件
     */
    @UiIntentObserver(DuplicateFinderUiIntent.SelectAllInGroup::class)
    private suspend fun onSelectAllInGroup(intent: DuplicateFinderUiIntent.SelectAllInGroup) {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return
        val searchState = state.searchState as? DuplicateFinderSearchState.Success ?: return

        val group = searchState.duplicateGroups.find { it.hash == intent.hash } ?: return
        val newSelected = state.selectedFiles.toMutableSet().apply {
            addAll(group.files)
        }

        state.copy(selectedFiles = newSelected).setup()
    }

    /**
     * 取消全选
     */
    @UiIntentObserver(DuplicateFinderUiIntent.DeselectAll::class)
    private suspend fun onDeselectAll() {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return
        state.copy(selectedFiles = emptySet()).setup()
    }

    /**
     * 删除选中文件
     */
    @UiIntentObserver(DuplicateFinderUiIntent.DeleteSelected::class)
    private suspend fun onDeleteSelected() {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return

        if (state.selectedFiles.isEmpty()) {
            AppViewEvent.PopupToastMessageByResId(R.string.no_files_selected).emit()
            return
        }

        // 显示确认对话框
        val confirmed = DuplicateFinderDialogState.DeleteConfirm(state.selectedFiles).run {
            popupAwaitDialogResult { deferredResult.awaitCompleted() }
        } ?: return

        if (!confirmed) return

        // 执行删除
        enqueueAsyncTask(Dispatchers.IO) {
            val currentState = getOrNull<DuplicateFinderUiState.Normal>() ?: return@enqueueAsyncTask

            currentState.selectedFiles.toList().deletes(
                onProgressUpdate = { deletedCount, totalCount ->
                    currentState.copy(
                        loadState = DuplicateFinderLoadState.Deleting(deletedCount, totalCount)
                    ).setup()
                }
            )

            // 删除完成后刷新搜索结果
            currentState.copy(
                loadState = DuplicateFinderLoadState.None,
                selectionMode = false,
                selectedFiles = emptySet()
            ).setup()

            // 重新搜索以更新结果
            onResetSearch()
        }
    }

    /**
     * 重置搜索状态
     */
    private suspend fun onResetSearch() {
        val state = getOrNull<DuplicateFinderUiState.Normal>() ?: return
        state.copy(
            searchState = DuplicateFinderSearchState.Idle,
            selectedFiles = emptySet(),
            selectionMode = false
        ).setup()
        onStartSearch()
    }

    /**
     * 递归收集所有文件
     */
    private suspend fun collectAllFiles(directory: File): List<File> = withContext(Dispatchers.IO) {
        val files = mutableListOf<File>()
        val stack = mutableListOf<File>()
        stack.add(directory)

        var scannedCount = 0
        val state = getOrNull<DuplicateFinderUiState.Normal>()

        while (stack.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val dir = stack.removeAt(stack.size - 1)

            dir.listFilteredFiles().forEach { file ->
                if (file.isFile) {
                    files.add(file)
                } else if (file.isDirectory) {
                    stack.add(file)
                }
            }

            scannedCount++
            state?.copy(
                loadState = DuplicateFinderLoadState.Scanning(
                    currentFile = dir,
                    scannedCount = scannedCount,
                    totalCount = scannedCount
                )
            )?.setup()
        }

        files
    }
}
