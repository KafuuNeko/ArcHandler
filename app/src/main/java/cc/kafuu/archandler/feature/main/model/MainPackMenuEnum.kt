package cc.kafuu.archandler.feature.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class MainPackMenuEnum(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
) {
    Cancel(R.drawable.ic_close, R.string.cancel),
    Pack(R.drawable.ic_packing, R.string.archive),
}
