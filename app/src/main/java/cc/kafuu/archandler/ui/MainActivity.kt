package cc.kafuu.archandler.ui

import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cc.kafuu.archandler.libs.core.CoreActivity
import cc.kafuu.archandler.ui.theme.AppTheme
import cc.kafuu.archandler.vm.MainViewModel

class MainActivity : CoreActivity() {
    private val mViewModel by viewModels<MainViewModel>()

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiState.collectAsState()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        Greeting("Android")
    }
}