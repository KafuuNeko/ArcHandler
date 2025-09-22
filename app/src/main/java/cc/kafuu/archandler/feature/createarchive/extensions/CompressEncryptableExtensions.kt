package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.capabilities.CompressEncryptable
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState

@Suppress("UNCHECKED_CAST")
fun <T> T.withPassword(password: String?): T
        where T : CreateArchiveOptionState, T : CompressEncryptable =
    when (this) {
        is CreateArchiveOptionState.Zip -> copy(password = password) as T
        is CreateArchiveOptionState.SevenZip -> copy(password = password) as T
        else -> this
    }