package cc.kafuu.archandler.feature.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class MainPackMenuEnum(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    Pack(R.drawable.ic_packing, R.string.archive),
    Cancel(R.drawable.ic_close, R.string.cancel),
}