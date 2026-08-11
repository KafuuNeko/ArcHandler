package cc.kafuu.archandler.feature.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class MainPasteMenuEnum(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
) {
    Paste(R.drawable.ic_paste, R.string.paste),
    Cancel(R.drawable.ic_close, R.string.cancel),
}
