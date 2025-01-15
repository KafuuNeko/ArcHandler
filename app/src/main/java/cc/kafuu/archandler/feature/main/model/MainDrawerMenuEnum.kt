package cc.kafuu.archandler.feature.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.kafuu.archandler.R

enum class MainDrawerMenuEnum(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    Code(R.drawable.ic_code, R.string.code_repository),
    Feedback(R.drawable.ic_feedback, R.string.feedback),
    Rate(R.drawable.ic_rate, R.string.rate),
    About(R.drawable.ic_about, R.string.about)
}