package cc.kafuu.archandler.feature.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class MainPasteMenuEnum(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    Paste(R.drawable.ic_paste, R.string.paste),
    Cancel(R.drawable.ic_close, R.string.cancel),
}