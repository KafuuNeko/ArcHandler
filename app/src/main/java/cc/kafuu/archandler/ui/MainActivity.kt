package cc.kafuu.archandler.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.ActivityPreview
import cc.kafuu.archandler.libs.core.CoreActivity
import cc.kafuu.archandler.libs.core.attachEventListener
import cc.kafuu.archandler.libs.ext.castOrNull
import cc.kafuu.archandler.libs.ext.getIcon
import cc.kafuu.archandler.libs.ext.getLastModifiedDate
import cc.kafuu.archandler.libs.ext.getReadableSize
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppLoadingView
import cc.kafuu.archandler.ui.widges.AppOptionalIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppPrimaryButton
import cc.kafuu.archandler.ui.widges.LazyList
import cc.kafuu.archandler.vm.MainDirectoryViewMode
import cc.kafuu.archandler.vm.MainSingleEvent
import cc.kafuu.archandler.vm.MainUiIntent
import cc.kafuu.archandler.vm.MainUiState
import cc.kafuu.archandler.vm.MainViewModel
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : CoreActivity() {
    private val mViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachEventListener(mViewModel) { onSingleEvent(it) }
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        uiState?.also { state ->
            BackHandler {
                if (drawerState.isOpen) {
                    coroutineScope.launch { drawerState.close() }
                    return@BackHandler
                }
                when (state) {
                    is MainUiState.DirectoryList -> onBackHandler(state)
                    else -> finish()
                }
            }
            MainViewBody(
                uiState = state,
                drawerState = drawerState,
                emitIntent = { intent -> mViewModel.emit(intent) }
            )
        } ?: mViewModel.emit(MainUiIntent.Init)
    }

    private fun onSingleEvent(singleEvent: MainSingleEvent) = when (singleEvent) {
        MainSingleEvent.JumpFilePermissionSetting -> onJumpFilePermissionSetting()
    }

    private fun onJumpFilePermissionSetting() {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request { _: List<String?>?, _: Boolean -> mViewModel.emit(MainUiIntent.Init) }
    }

    private fun onBackHandler(state: MainUiState.DirectoryList) {
        if (state.loadingState.isLoading) return
        MainUiIntent.BackToParent(
            storageData = state.storageData,
            currentPath = state.directoryPath
        ).also {
            mViewModel.emit(it)
        }
    }
}

@Composable
private fun MainViewBody(
    uiState: MainUiState,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    when (uiState) {
        MainUiState.PermissionDenied -> PermissionDenied(
            emitIntent = emitIntent
        )

        is MainUiState.StorageVolumeList -> MainLayout(
            uiState = uiState,
            drawerState = drawerState,
            emitIntent = emitIntent
        ) {
            StorageVolumeList(
                uiState = uiState,
                emitIntent = emitIntent
            )
        }

        is MainUiState.DirectoryList -> MainLayout(
            uiState = uiState,
            drawerState = drawerState,
            emitIntent = emitIntent
        ) {
            DirectoryView(
                uiState = uiState,
                emitIntent = emitIntent
            )
        }
    }
}

@Composable
private fun PermissionDenied(
    modifier: Modifier = Modifier,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(64.dp),
            painter = painterResource(R.drawable.ic_folder_off),
            contentDescription = stringResource(R.string.not_permission_in_main_view)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.not_permission_in_main_view),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                emitIntent(MainUiIntent.JumpFilePermissionSetting)
            },
            text = stringResource(R.string.enable_permission),
        )
    }
}

@Composable
private fun MainLayout(
    uiState: MainUiState,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val title = when (uiState) {
        is MainUiState.DirectoryList -> {
            uiState.viewMode.castOrNull<MainDirectoryViewMode.MultipleSelect>()?.let {
                stringResource(R.string.n_files_selected, it.selected.size)
            } ?: uiState.storageData.name
        }

        else -> stringResource(R.string.app_name)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainScaffoldDrawer(
                drawerState = drawerState,
                emitIntent = emitIntent
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .statusBarsPadding(),
            topBar = {
                MainScaffoldTopBar(
                    title = title,
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                content()
                uiState.loadingState.takeIf {
                    it.isLoading
                }?.let {
                    AppLoadingView(loadingState = uiState.loadingState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffoldTopBar(
    title: String,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            Image(
                modifier = Modifier
                    .size(50.dp)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .clickable { onMenuClick() },
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = stringResource(R.string.home_menu)
            )
        },
    )
}

@Composable
private fun MainScaffoldDrawer(
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(220.dp),
        drawerState = drawerState
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DrawerHeader()

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            DrawerMenuList(emitIntent = emitIntent)
        }
    }
}

@Composable
private fun DrawerHeader() {
    val appVersionName = LocalContext.current.run {
        packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
    } ?: stringResource(R.string.unknown_version)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Image(
            modifier = Modifier.size(64.dp),
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = appVersionName,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = AppModel.EMAIL,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DrawerMenuList(
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column {
        DrawerMenuOption(
            icon = R.drawable.ic_code,
            title = stringResource(R.string.code_repository)
        ) {
            emitIntent(MainUiIntent.CodeRepositoryClick)
        }

        DrawerMenuOption(
            icon = R.drawable.ic_feedback,
            title = stringResource(R.string.feedback)
        ) {
            emitIntent(MainUiIntent.FeedbackClick)
        }

        DrawerMenuOption(
            icon = R.drawable.ic_rate,
            title = stringResource(R.string.rate)
        ) {
            emitIntent(MainUiIntent.RateClick)
        }

        DrawerMenuOption(
            icon = R.drawable.ic_aboutt,
            title = stringResource(R.string.about)
        ) {
            emitIntent(MainUiIntent.AboutClick)
        }
    }
}

@Composable
private fun DrawerMenuOption(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onMenuClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(icon),
            contentDescription = title
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun StorageVolumeList(
    modifier: Modifier = Modifier,
    uiState: MainUiState.StorageVolumeList,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.storage_volume),
            style = MaterialTheme.typography.headlineMedium
        )
        LazyList(
            modifier = Modifier
                .padding(top = 10.dp),
            emptyState = {
                if (uiState.loadingState.isLoading) return@LazyList
                FillMessage(
                    icon = painterResource(R.drawable.ic_storage),
                    message = stringResource(R.string.no_accessible_storage_devices),
                )
            },
            items = uiState.storageVolumes
        ) {
            AppIconTextItemCard(
                modifier = Modifier
                    .fillMaxWidth(),
                painter = painterResource(R.drawable.ic_storage),
                text = it.name,
                secondaryText = it.directory.path
            ) {
                emitIntent(MainUiIntent.StorageVolumeSelected(it))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DirectoryView(
    modifier: Modifier = Modifier,
    uiState: MainUiState.DirectoryList,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 10.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 10.dp),
            text = uiState.directoryPath.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
        FileList(
            modifier = Modifier
                .fillMaxHeight(),
            uiState = uiState,
            emitIntent = emitIntent
        )
    }
}

@Composable
private fun FileList(
    modifier: Modifier = Modifier,
    uiState: MainUiState.DirectoryList,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
    ) {
        LazyList(
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp)
                .weight(1f),
            items = uiState.files,
            emptyState = {
                if (uiState.loadingState.isLoading) return@LazyList
                FillMessage(
                    icon = painterResource(R.drawable.ic_empty_folder),
                    message = stringResource(R.string.empty_directory),
                )
            }
        ) { file ->
            val (multipleSelectMode, selectedSet) =
                uiState.viewMode.castOrNull<MainDirectoryViewMode.MultipleSelect>()?.let {
                    true to it.selected
                } ?: (false to null)

            FileItem(
                storageData = uiState.storageData,
                file = file,
                multipleSelectMode = multipleSelectMode,
                selectedSet = selectedSet,
                emitIntent = emitIntent
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        uiState.viewMode.castOrNull<MainDirectoryViewMode.MultipleSelect>()?.let {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            DirectoryListMultipleMenu(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                viewMode = it,
                emitIntent = emitIntent
            )
        }

        uiState.viewMode.castOrNull<MainDirectoryViewMode.Pause>()?.let {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            DirectoryListPauseMenu(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                viewMode = it,
                emitIntent = emitIntent
            )
        }
    }
}

@Composable
private fun FileItem(
    storageData: StorageData,
    file: File,
    multipleSelectMode: Boolean,
    selectedSet: Set<File>?,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    val text = file.name
    val secondaryText = file.takeIf { it.isFile }?.let {
        stringResource(
            R.string.file_info_format,
            file.getLastModifiedDate(), file.getReadableSize()
        )
    }
    val checked = selectedSet?.contains(file) ?: false
    val onCheckedChange = {
        MainUiIntent.FileCheckedChange(
            file = file,
            checked = !checked
        ).also(emitIntent)
    }
    AppOptionalIconTextItemCard(
        modifier = Modifier
            .fillMaxWidth(),
        painter = painterResource(file.getIcon()),
        text = text,
        checked = checked,
        secondaryText = secondaryText,
        displaySelectBox = multipleSelectMode,
        onCheckedChange = { onCheckedChange() },
        onLongClick = { emitIntent(MainUiIntent.FileMultipleSelectMode(!multipleSelectMode)) }
    ) {
        if (multipleSelectMode) {
            onCheckedChange()
            return@AppOptionalIconTextItemCard
        }
        MainUiIntent.FileSelected(
            storageData = storageData,
            file = file
        ).also(emitIntent)
    }
}

@Composable
private fun DirectoryListMultipleMenu(
    modifier: Modifier = Modifier,
    viewMode: MainDirectoryViewMode.MultipleSelect,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        // TODO: 待实现多选菜单功能
    }
}

@Composable
private fun DirectoryListPauseMenu(
    modifier: Modifier = Modifier,
    viewMode: MainDirectoryViewMode.Pause,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        // TODO: 待实现粘贴模式菜单功能
    }
}

@Composable
private fun FillMessage(
    icon: Painter,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(96.dp),
            painter = icon,
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun PermissionDeniedBodyPreview() {
    ActivityPreview(darkTheme = true) {
        PermissionDenied()
    }
}