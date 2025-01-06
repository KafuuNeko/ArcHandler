package cc.kafuu.archandler.libs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import androidx.annotation.StringRes
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.model.StorageData
import org.koin.core.component.KoinComponent
import java.io.File

class AppLibs(
    private val mContext: Context
) : KoinComponent {
    companion object {
        private const val TAG = "AppLibs"
    }

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

    fun getMountedStorageVolumes() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val storageManager = mContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        storageManager.storageVolumes.mapNotNull { volumes ->
            StorageData(
                name = volumes.mediaStoreVolumeName ?: getString(R.string.unknown),
                directory = volumes.directory ?: return@mapNotNull null
            )
        }
    } else {
        val storageVolumes = mutableListOf<StorageData>()

        File(Environment.getExternalStorageDirectory().path).takeIf {
            it.exists()
        }?.let { file ->
            StorageData(
                name = getString(R.string.internal_storage),
                directory = file
            )
        }?.also { storage ->
            storageVolumes.add(storage)
        }

        val storageRoot = File("/storage/").listFiles()?.toList() ?: emptyList()
        val mntRoot = File("/mnt/").listFiles()?.toList() ?: emptyList()
        (storageRoot + mntRoot).filter { file ->
            file.exists() && file.isDirectory && file.canRead() && !file.name.contains("emulated")
        }.forEach { dir ->
            storageVolumes.add(StorageData(name = dir.name, directory = dir))
        }

        storageVolumes
    }.also {
        Log.d(TAG, "getMountedStorageVolumes: $it")
    }
}