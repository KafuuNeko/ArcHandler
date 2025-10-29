package cc.kafuu.archandler.feature.storagepicker

import android.app.Activity
import android.content.Intent
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.storagepicker.model.PickMode
import cc.kafuu.archandler.feature.storagepicker.model.StoragePickerParams
import cc.kafuu.archandler.feature.storagepicker.model.StoragePickerResult
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerListState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerLoadState
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiIntent
import cc.kafuu.archandler.feature.storagepicker.presention.StoragePickerUiState
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.AppViewEvent
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.extensions.getParentPath
import cc.kafuu.archandler.libs.extensions.isSameFileOrDirectory
import cc.kafuu.archandler.libs.manager.DataTransferManager
import cc.kafuu.archandler.libs.manager.FileManager
import cc.kafuu.archandler.libs.model.StorageData
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

class StoragePickerViewModel :
    CoreViewModelWithEvent<StoragePickerUiIntent, StoragePickerUiState>(StoragePickerUiState.None),
    KoinComponent {

    private val _dataTransferManager by inject<DataTransferManager>()

    /**
     * 加载挂载的存储设备
     */
    private suspend fun loadExternalStorages() {
        val uiStatePrototype = getOrNull<StoragePickerUiState.Normal>() ?: return
        uiStatePrototype.copy(loadState = StoragePickerLoadState.ExternalStoragesLoading).setup()
        runCatching {
            withContext(Dispatchers.IO) { get<FileManager>().getMountedStorageVolumes() }
        }.onSuccess { storages ->
            uiStatePrototype.copy(
                listState = StoragePickerListState.StorageVolume(storageVolumes = storages)
            ).setup()
        }.onFailure { exception ->
            uiStatePrototype.setup()
            val message = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            AppViewEvent.PopupToastMessage(message).emit()
        }
    }

    /**
     * 加载目录信息
     */
    private suspend fun doLoadDirectory(
        storageData: StorageData,
        directoryPath: Path
    ) {
        val uiStatePrototype = getOrNull<StoragePickerUiState.Normal>() ?: return
        uiStatePrototype.copy(loadState = StoragePickerLoadState.DirectoryLoading).setup()
        val directory = File(directoryPath.toString())
        runCatching {
            if (!directory.canRead()) return@runCatching emptyList()
            val isShowHiddenFiles = AppModel.isShowHiddenFiles
            val isShowUnreadableDirectories = AppModel.isShowUnreadableDirectories
            val isShowUnreadableFiles = AppModel.isShowUnreadableFiles
            withContext(Dispatchers.IO) {
                File(directoryPath.toString())
                    .listFiles()
                    ?.asList()
                    ?.filter {
                        if (uiStatePrototype.pickMode == PickMode.ChooseDirectory) it.isDirectory else true
                    }
                    ?.filter {
                        return@filter !(it == null ||
                                (!isShowHiddenFiles && it.name.startsWith(".")) ||
                                (!isShowUnreadableDirectories && it.isDirectory && !it.canRead()) ||
                                (!isShowUnreadableFiles && it.isFile && !it.canRead()))
                    }
                    ?: emptyList()
            }
        }.onSuccess { files ->
            uiStatePrototype.copy(
                listState = StoragePickerListState.Directory(
                    storageData = storageData,
                    directoryPath = directoryPath,
                    files = files,
                    canRead = directory.canRead(),
                    canWrite = directory.canWrite()
                )
            ).setup()
        }.onFailure { exception ->
            uiStatePrototype.setup()
            val message = exception.message ?: get<AppLibs>().getString(R.string.unknown_error)
            AppViewEvent.PopupToastMessage(message).emit()
        }
    }

    @UiIntentObserver(StoragePickerUiIntent.Init::class)
    suspend fun onInit(intent: StoragePickerUiIntent.Init) {
        if (!isStateOf<StoragePickerUiState.None>()) return
        if (!XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)) {
            StoragePickerUiState.Finished.setup()
            return
        }
        val initParams = _dataTransferManager.takeAs<StoragePickerParams>(intent.paramsToken)
        if (initParams == null) return
        StoragePickerUiState.Normal(pickMode = initParams.pickMode).setup()
        if (initParams.defaultStorage == null) {
            loadExternalStorages()
        } else if (initParams.defaultPath == null) {
            doLoadDirectory(
                storageData = initParams.defaultStorage,
                directoryPath = Path(initParams.defaultStorage.directory.path)
            )
        } else {
            doLoadDirectory(
                storageData = initParams.defaultStorage,
                directoryPath = Path(initParams.defaultPath)
            )
        }
    }

    @UiIntentObserver(StoragePickerUiIntent.Back::class)
    suspend fun onBack() {
        val state = getOrNull<StoragePickerUiState.Normal>() ?: return
        when (state.listState) {
            StoragePickerListState.Undecided -> Unit

            is StoragePickerListState.Directory -> {
                val currentPath = state.listState.directoryPath
                val storageData = state.listState.storageData
                val parent = currentPath.getParentPath()
                if (parent == null || Path(storageData.directory.path).isSameFileOrDirectory(
                        currentPath
                    )
                ) {
                    loadExternalStorages()
                } else {
                    doLoadDirectory(storageData, parent)
                }
            }

            is StoragePickerListState.StorageVolume -> {
                AppViewEvent.SetResult(Activity.RESULT_CANCELED).emit()
                StoragePickerUiState.Finished.setup()
            }
        }
    }

    @UiIntentObserver(StoragePickerUiIntent.ClosePage::class)
    suspend fun onClosePage() {
        AppViewEvent.SetResult(Activity.RESULT_CANCELED).emit()
        StoragePickerUiState.Finished.setup()
    }

    @UiIntentObserver(StoragePickerUiIntent.FileSelected::class)
    suspend fun onFileSelected(intent: StoragePickerUiIntent.FileSelected) {
        if (intent.file.isDirectory && intent.file.canRead()) {
            doLoadDirectory(intent.storageData, Path(intent.file.path))
        }
    }

    @UiIntentObserver(StoragePickerUiIntent.SelectionCompleted::class)
    suspend fun onSelectionCompleted() {
        val state = getOrNull<StoragePickerUiState.Normal>() ?: return
        val listState = state.listState as? StoragePickerListState.Directory ?: return
        val token = _dataTransferManager.push(
            StoragePickerResult.ChooseDirectory(listState.storageData, listState.directoryPath)
        )
        val intent = Intent().apply {
            putExtra(AppModel.KEY_DATA, token)
        }
        AppViewEvent.SetResult(Activity.RESULT_OK, intent).emit()
        StoragePickerUiState.Finished.setup()
    }
}