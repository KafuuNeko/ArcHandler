package cc.kafuu.archandler.feature.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.kafuu.archandler.R


enum class MainMultipleMenuEnum(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    Copy(R.drawable.ic_file_copy, R.string.copy),
    Move(R.drawable.ic_file_moving, R.string.move),
    Delete(R.drawable.ic_delete, R.string.delete),
    Archive(R.drawable.ic_packing, R.string.archive),
}