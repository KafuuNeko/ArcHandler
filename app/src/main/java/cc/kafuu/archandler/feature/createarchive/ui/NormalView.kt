package cc.kafuu.archandler.feature.createarchive.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressLevelConfigurable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressEncryptable
import cc.kafuu.archandler.feature.createarchive.capabilities.CompressSplittable
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiIntent
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveUiState
import cc.kafuu.archandler.feature.createarchive.ui.cards.FormatCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.LevelCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.PasswordCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.SavePathCard
import cc.kafuu.archandler.feature.createarchive.ui.cards.SplitCard
import cc.kafuu.archandler.ui.widges.AppTopBar

@Composable
fun NormalView(
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
                .verticalScroll(rememberScrollState())
        ) {
            ArchiveOptions(uiState = uiState, emitIntent = emitIntent)
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
        onPick = { CreateArchiveUiIntent.PickSavePath.also(emitIntent) },
        onNameChange = { CreateArchiveUiIntent.TargetFileNameChange(it).also(emitIntent) }
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (optionState is CompressLevelConfigurable) {
        LevelCard(
            range = optionState.format.levelRange ?: 0..0,
            level = optionState.level,
            onLevelChange = { CreateArchiveUiIntent.CompressLevelChange(it).also(emitIntent) }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    if (optionState is CompressEncryptable) {
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

    if (optionState is CompressSplittable) {
        SplitCard(
            enabled = optionState.splitEnabled,
            number = optionState.splitSize,
            unit = optionState.splitUnit,
            onToggle = {
                CreateArchiveUiIntent.SplitEnabledToggle(it).also(emitIntent)
            },
            onUnitChange = {
                CreateArchiveUiIntent.SplitUnitChange(it).also(emitIntent)
            },
            onNumberChange = {
                CreateArchiveUiIntent.SplitSizeChange(it).also(emitIntent)
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}