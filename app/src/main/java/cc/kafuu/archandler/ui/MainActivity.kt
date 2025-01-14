package cc.kafuu.archandler.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import cc.kafuu.archandler.libs.model.LoadingState
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.widges.AppIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppLoadingView
import cc.kafuu.archandler.ui.widges.AppOptionalIconTextItemCard
import cc.kafuu.archandler.ui.widges.AppPrimaryButton
import cc.kafuu.archandler.ui.widges.LazyList
import cc.kafuu.archandler.vm.MainDrawerMenu
import cc.kafuu.archandler.vm.MainListData
import cc.kafuu.archandler.vm.MainListViewMode
import cc.kafuu.archandler.vm.MainMultipleMenu
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
                // 如果抽屉打开优先关闭抽屉
                if (drawerState.isOpen) {
                    coroutineScope.launch { drawerState.close() }
                    return@BackHandler
                }
                when (state) {
                    is MainUiState.Accessible -> onBackHandler(state)
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

    private fun onBackHandler(state: MainUiState.Accessible) {
        if (state.loadingState.isLoading) return

        when (state.viewMode) {
            MainListViewMode.Normal -> {
                state.listData.castOrNull<MainListData.Directory>()?.let {
                    doBackToParent(it)
                } ?: finish()
            }

            is MainListViewMode.MultipleSelect -> mViewModel.emit(
                MainUiIntent.BackToNormalViewMode
            )

            is MainListViewMode.Pause -> {
                state.listData.castOrNull<MainListData.Directory>()?.let {
                    doBackToParent(it)
                } ?: mViewModel.emit(MainUiIntent.BackToNormalViewMode)
            }
        }
    }

    private fun doBackToParent(listData: MainListData.Directory) {
        MainUiIntent.BackToParent(
            storageData = listData.storageData,
            currentPath = listData.directoryPath
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

        is MainUiState.Accessible -> MainLayout(
            uiState = uiState,
            drawerState = drawerState,
            emitIntent = emitIntent
        ) {
            AccessibleView(
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
    uiState: MainUiState.Accessible,
    drawerState: DrawerState,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val title = when (val listData = uiState.listData) {
        is MainListData.Directory -> {
            uiState.viewMode.castOrNull<MainListViewMode.MultipleSelect>()?.let {
                stringResource(R.string.n_files_selected, it.selected.size)
            } ?: listData.storageData.name
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
        MainDrawerMenu.entries.forEach {
            DrawerMenuOption(
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                emitIntent(MainUiIntent.MainDrawerMenuClick(it))
            }
        }
    }
}

@Composable
private fun DrawerMenuOption(
    modifier: Modifier = Modifier,
    icon: Painter,
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
            painter = icon,
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
private fun AccessibleView(
    modifier: Modifier = Modifier,
    uiState: MainUiState.Accessible,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    when (val listData = uiState.listData) {
        MainListData.Undecided -> Unit

        is MainListData.StorageVolume -> StorageVolumeList(
            modifier = modifier,
            loadingState = uiState.loadingState,
            listData = listData,
            emitIntent = emitIntent
        )

        is MainListData.Directory -> DirectoryView(
            modifier = modifier,
            loadingState = uiState.loadingState,
            listData = listData,
            viewMode = uiState.viewMode,
            emitIntent = emitIntent
        )
    }
}

@Composable
private fun StorageVolumeList(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    listData: MainListData.StorageVolume,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.storage_volume),
            style = MaterialTheme.typography.headlineMedium
        )
        LazyList(
            modifier = Modifier
                .padding(top = 10.dp),
            emptyState = {
                if (loadingState.isLoading) return@LazyList
                FillMessage(
                    icon = painterResource(R.drawable.ic_storage),
                    message = stringResource(R.string.no_accessible_storage_devices),
                )
            },
            items = listData.storageVolumes
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
    loadingState: LoadingState,
    listData: MainListData.Directory,
    viewMode: MainListViewMode,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 10.dp),
            text = listData.directoryPath.toString(),
            style = MaterialTheme.typography.headlineMedium
        )

        LazyList(
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp)
                .weight(1f),
            items = listData.files,
            emptyState = {
                if (loadingState.isLoading) return@LazyList
                FillMessage(
                    icon = painterResource(R.drawable.ic_empty_folder),
                    message = stringResource(R.string.empty_directory),
                )
            }
        ) { file ->
            val selectedSet = viewMode.castOrNull<MainListViewMode.MultipleSelect>()?.selected
            FileItem(
                storageData = listData.storageData,
                file = file,
                multipleSelectMode = selectedSet != null,
                selectedSet = selectedSet,
                emitIntent = emitIntent
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (viewMode !is MainListViewMode.Normal) {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }

        when (viewMode) {
            MainListViewMode.Normal -> Unit

            is MainListViewMode.MultipleSelect -> DirectoryListMultipleMenu(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 10.dp),
                listData = listData,
                viewMode = viewMode,
                emitIntent = emitIntent
            )

            is MainListViewMode.Pause -> DirectoryListPauseMenu(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                viewMode = viewMode,
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
    listData: MainListData.Directory,
    viewMode: MainListViewMode.MultipleSelect,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
    ) {
        val files = viewMode.selected.toList()
        MainMultipleMenu.entries.forEach {
            BottomMenu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = painterResource(it.icon),
                title = stringResource(it.title)
            ) {
                MainUiIntent.MultipleMenuClick(
                    menu = it,
                    sourceStorageData = listData.storageData,
                    sourceDirectoryPath = listData.directoryPath,
                    sourceFiles = files,
                ).also(emitIntent)
            }
        }
    }
}

@Composable
private fun DirectoryListPauseMenu(
    modifier: Modifier = Modifier,
    viewMode: MainListViewMode.Pause,
    emitIntent: (uiIntent: MainUiIntent) -> Unit = {},
) {
    Row(modifier = modifier) {
        // TODO: 待实现粘贴模式菜单功能
    }
}

@Composable
private fun BottomMenu(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onMenuClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .size(30.dp),
            painter = icon,
            contentDescription = title
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
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