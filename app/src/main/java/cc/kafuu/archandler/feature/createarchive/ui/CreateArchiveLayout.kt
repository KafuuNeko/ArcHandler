package cc.kafuu.archandler.feature.createarchive.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveLoadState
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.feature.createarchive.ui.cards.CompressionTypeCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.FormatCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.LevelCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.PasswordCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.SavePathCard
import cc.kafuu.archandler.libs.model.StorageData
import cc.kafuu.archandler.ui.dialogs.AppLoadDialog
import cc.kafuu.archandler.ui.widges.AppTopBar
import java.io.File

@Composable
fun CreateArchiveLayout(
    uiState: CreateArchiveUiState,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit = {}
) {
    when (uiState) {
        CreateArchiveUiState.None, CreateArchiveUiState.Finished -> Unit
        is CreateArchiveUiState.Normal -> {
            NormalView(uiState, emitIntent)
            LoadDialogSwitch(uiState.loadState, emitIntent)
        }
    }
}

@Composable
private fun LoadDialogSwitch(
    loadState: CreateArchiveLoadState,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit
) {
    when (loadState) {
        CreateArchiveLoadState.None -> Unit
        is CreateArchiveLoadState.Packing -> {
            val message = stringResource(loadState.message)
            val filename = loadState.currentFile?.name
            val progress = loadState.progression?.let { "${it.first}/${it.second}" }
            AppLoadDialog(
                messages = listOf(message, filename, progress).mapNotNull { it },
                buttonText = stringResource(R.string.cancel),
                onClickButton = {
                    emitIntent(CreateArchiveUiIntent.CancelPackingJob)
                }
            )
        }
    }

}

@Composable
private fun NormalView(
    uiState: CreateArchiveUiState.Normal,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.create_archive),
                onBack = { emitIntent(CreateArchiveUiIntent.Back) }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                ArchiveOptions(uiState = uiState, emitIntent = emitIntent)
            }

            Spacer(modifier = Modifier.height(10.dp))

            FilledIconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 10.dp),
                onClick = {
                    CreateArchiveUiIntent.CreateArchive.also(emitIntent)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.ic_packing),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.create_archive))
                }
            }
        }
    }
}

@Composable
private fun ArchiveOptions(
    uiState: CreateArchiveUiState.Normal,
    emitIntent: (uiIntent: CreateArchiveUiIntent) -> Unit
) {
    val optionState = uiState.archiveOptions
    // 选择压缩包格式
    FormatCard(
        format = optionState.format,
        onFormatChange = {
            CreateArchiveUiIntent.ArchiveFormatChange(it).also(emitIntent)
        }
    )
    Spacer(modifier = Modifier.height(10.dp))

    // 选择保存路径
    SavePathCard(
        dir = uiState.targetDirectory,
        outputName = uiState.targetFileName,
        onNameChange = { CreateArchiveUiIntent.TargetFileNameChange(it).also(emitIntent) }
    ) {
        CreateArchiveUiIntent.SelectFolder.also(emitIntent)
    }
    Spacer(modifier = Modifier.height(10.dp))

    // 选择压缩包压缩类型
    CompressionTypeCard(
        supportCompressionTypes = optionState.format.supportCompressionTypes,
        compressionType = optionState.compressionType,
        onFormatChange = {
            CreateArchiveUiIntent.ArchiveCompressionTypeChange(it).also(emitIntent)
        }
    )
    Spacer(modifier = Modifier.height(10.dp))

    // 压缩等级
    if (optionState.compressionType.levelRange != null) {
        LevelCard(
            range = optionState.compressionType.levelRange,
            level = optionState.level,
            onLevelChange = { CreateArchiveUiIntent.CompressLevelChange(it).also(emitIntent) }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    // 压缩包密码
    if (optionState.format.supportsPassword) {
        PasswordCard(
            password = optionState.password ?: "",
            onPasswordChange = {
                CreateArchiveUiIntent.ArchivePasswordChange(it).also(emitIntent)
            },
            onClear = {
                CreateArchiveUiIntent.ArchivePasswordChange("").also(emitIntent)
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun ArchiveOptionsViewPreview() {
    CreateArchiveLayout(
        uiState = CreateArchiveUiState.Normal(emptyList(), StorageData(), File("")),
        emitIntent = {}
    )
}