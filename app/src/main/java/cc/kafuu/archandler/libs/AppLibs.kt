package cc.kafuu.archandler.libs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.StringRes
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.model.AppInfo
import org.koin.core.component.KoinComponent
import androidx.core.net.toUri

class AppLibs(
    private val mContext: Context
) : KoinComponent {
    fun getString(@StringRes id: Int, vararg args: Any): String {
        return mContext.resources?.getString(id, *args).toString()
    }

    fun getVersionName(): String {
        return mContext.packageManager.getPackageInfo(mContext.packageName, 0)
            .versionName ?: getString(R.string.unknown_version)
    }

    fun jumpToUrl(url: String) {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            mContext.startActivity(it)
        }
    }

    /**
     * 获取应用的显示名称
     */
    fun getAppDisplayName(packageName: String?): String {
        return if (packageName == null) {
            mContext.getString(R.string.default_app_always_ask)
        } else {
            try {
                val pm = mContext.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                mContext.getString(R.string.default_app_system_default)
            }
        }
    }

    /**
     * 获取可以处理指定 MIME 类型的所有应用
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getAppsForMimeType(mimeTypes: List<String>): List<AppInfo> {
        val pm = mContext.packageManager
        val selfPackageName = mContext.packageName

        val result = mutableSetOf<String>()

        mimeTypes.forEach { mimeType ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType("content://test".toUri(), mimeType)
                addCategory(Intent.CATEGORY_DEFAULT)
            }

            val resolveInfos =
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

            resolveInfos.forEach {
                val pkg = it.activityInfo.packageName
                if (pkg != selfPackageName) {
                    result.add(pkg)
                }
            }
        }

        return result.mapNotNull { createAppInfo(pm, it) }
    }

    private fun createAppInfo(pm: PackageManager, packageName: String): AppInfo? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            AppInfo(
                packageName = packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                icon = pm.getApplicationIcon(packageName)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}