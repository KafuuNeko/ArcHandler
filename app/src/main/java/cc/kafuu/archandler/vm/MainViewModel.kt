package cc.kafuu.archandler.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.core.CoreViewModel
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path

class MainViewModel : CoreViewModel<MainUiIntent, MainUiState, MainSingleEvent>(), KoinComponent {
    override fun onCollectedIntent(uiIntent: MainUiIntent) {
        when (uiIntent) {
            MainUiIntent.Init -> initViewModel()

            MainUiIntent.JumpFilePermissionSetting -> dispatchingEvent(
                MainSingleEvent.JumpFilePermissionSetting
            )

            is MainUiIntent.ChangeDirectory -> loadDirectory(uiIntent.directoryPath)
        }
    }

    private fun initViewModel() {
        val context = get<Context>()
        if (!XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE)) {
            updateState(MainUiState.NotPermission)
            return
        }
        loadDirectory(Path(context.filesDir.path))
    }

    private fun loadDirectory(
        directoryPath: Path
    ) = viewModelScope.launch(Dispatchers.IO) {
        updateState(MainUiState.DirectoryLoading(directoryPath))
        try {
            val files = File(directoryPath.toString()).listFiles()?.asList()
            MainUiState.Directory(
                directoryPath = directoryPath,
                files = files ?: emptyList(),
                multipleSelectMode = false,
                selectedSet = emptySet()
            ).run {
                updateState(this)
            }
        } catch (e: IOException) {
            MainUiState.DirectoryLoadFailure(
                directoryPath = directoryPath,
                message = e.message ?: get<AppLibs>().getString(R.string.unknown_error)
            ).run {
                updateState(this)
            }
        }
    }
}

sealed class MainUiIntent {
    data object Init : MainUiIntent()
    data object JumpFilePermissionSetting : MainUiIntent()
    data class ChangeDirectory(
        val directoryPath: Path
    ) : MainUiIntent()
}

sealed class MainUiState {
    // User has not authorized 'MANAGE_EXTERNAL_STORAGE'
    data object NotPermission : MainUiState()

    data class DirectoryLoading(
        val directoryPath: Path,
    ) : MainUiState()

    data class DirectoryLoadFailure(
        val directoryPath: Path,
        val message: String
    ) : MainUiState()

    data class Directory(
        val directoryPath: Path,
        val files: List<File>,
        val multipleSelectMode: Boolean,
        val selectedSet: Set<Path>,
    ) : MainUiState()
}

sealed class MainSingleEvent {
    data object JumpFilePermissionSetting : MainSingleEvent()
}