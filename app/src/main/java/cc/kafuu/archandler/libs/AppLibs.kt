package cc.kafuu.archandler.libs

import android.content.Context
import androidx.annotation.StringRes
import org.koin.core.component.KoinComponent

class AppLibs(
    private val mContext: Context
) : KoinComponent {
    fun getString(@StringRes id: Int, vararg args: Any): String {
        return mContext.resources?.getString(id, *args).toString()
    }
}