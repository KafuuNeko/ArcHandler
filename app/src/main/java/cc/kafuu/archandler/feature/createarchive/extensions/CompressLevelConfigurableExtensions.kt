package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.capabilities.CompressLevelConfigurable
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState

@Suppress("UNCHECKED_CAST")
fun <T> T.withLevel(level: Int): T
        where T : CreateArchiveOptionState, T : CompressLevelConfigurable =
    when (this) {
        is CreateArchiveOptionState.Zip -> copy(level = level) as T
        is CreateArchiveOptionState.SevenZip -> copy(level = level) as T
        is CreateArchiveOptionState.TarWithGZip -> copy(level = level) as T
        is CreateArchiveOptionState.BZip2 -> copy(level = level) as T
        else -> this
    }