package cc.kafuu.archandler.feature.createarchive.extensions

import cc.kafuu.archandler.feature.createarchive.capabilities.CompressSplittable
import cc.kafuu.archandler.feature.createarchive.presentation.CreateArchiveOptionState
import cc.kafuu.archandler.libs.archive.model.SplitUnit

@Suppress("UNCHECKED_CAST")
fun <T> T.withSplitEnable(
    enabled: Boolean
): T where T : CreateArchiveOptionState, T : CompressSplittable =
    when (this) {
        is CreateArchiveOptionState.Zip -> copy(splitEnabled = enabled)
        is CreateArchiveOptionState.SevenZip -> copy(splitEnabled = enabled)
        else -> this
    } as T

@Suppress("UNCHECKED_CAST")
fun <T> T.withSplitSize(
    size: Long? = null
): T where T : CreateArchiveOptionState, T : CompressSplittable =
    when (this) {
        is CreateArchiveOptionState.Zip -> copy(splitSize = size)
        is CreateArchiveOptionState.SevenZip -> copy(splitSize = size)
        else -> this
    } as T

@Suppress("UNCHECKED_CAST")
fun <T> T.withSplitUnit(
    unit: SplitUnit = SplitUnit.MB
): T where T : CreateArchiveOptionState, T : CompressSplittable =
    when (this) {
        is CreateArchiveOptionState.Zip -> copy(splitUnit = unit)
        is CreateArchiveOptionState.SevenZip -> copy(splitUnit = unit)
        else -> this
    } as T