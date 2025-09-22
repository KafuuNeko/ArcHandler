package cc.kafuu.archandler.feature.createarchive.capabilities

import cc.kafuu.archandler.libs.archive.model.SplitUnit

interface CompressSplittable {
    val splitEnabled: Boolean
    val splitSize: Long?
    val splitUnit: SplitUnit
}