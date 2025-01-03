package cc.kafuu.archandler.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.core.ActivityPreview
import cc.kafuu.archandler.libs.core.CoreActivity
import cc.kafuu.archandler.libs.core.attachEventListener
import cc.kafuu.archandler.ui.theme.AppTheme
import cc.kafuu.archandler.ui.widges.AppPrimaryButton
import cc.kafuu.archandler.vm.MainSingleEvent
import cc.kafuu.archandler.vm.MainUiIntent
import cc.kafuu.archandler.vm.MainUiState
import cc.kafuu.archandler.vm.MainViewModel
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions


class MainActivity : CoreActivity() {
    private val mViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachEventListener(mViewModel) { onSingleEvent(it) }
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiState.collectAsState()
        when (val state = uiState) {
            null -> mViewModel.emit(MainUiIntent.Init)

            is MainUiState.Directory -> MainViewBody(
                uiState = state
            )

            is MainUiState.DirectoryLoadFailure -> TODO()

            is MainUiState.DirectoryLoading -> TODO()

            MainUiState.NotPermission -> NotPermissionViewBody(
                onClick = { mViewModel.emit(it) }
            )
        }
    }

    private fun onSingleEvent(singleEvent: MainSingleEvent) = when (singleEvent) {
        MainSingleEvent.JumpFilePermissionSetting -> onJumpFilePermissionSetting()
    }

    private fun onJumpFilePermissionSetting() {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request { _: List<String?>?, _: Boolean -> mViewModel.emit(MainUiIntent.Init) }
    }
}


@Composable
private fun MainViewBody(
    uiState: MainUiState.Directory
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun NotPermissionViewBody(
    modifier: Modifier = Modifier,
    onClick: (uiIntent: MainUiIntent) -> Unit = {}
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
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onClick(MainUiIntent.JumpFilePermissionSetting)
            },
            text = stringResource(R.string.enable_permission),
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun NotPermissionViewBodyPreview() {
    ActivityPreview(darkTheme = true) {
        NotPermissionViewBody()
    }
}