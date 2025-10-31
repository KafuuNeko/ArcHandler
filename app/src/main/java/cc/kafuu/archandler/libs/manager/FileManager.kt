package cc.kafuu.archandler.libs.manager

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.model.StorageData
import java.io.File

class FileManager(private val mContext: Context) {
    companion object {
        private const val TAG = "FileManager"
    }

    private fun getUserStorage() = StorageData(
        name = mContext.getString(R.string.app_name),
        directory = File(mContext.filesDir, "user").apply {
            if (!exists()) mkdirs()
        }
    )

    fun getMountedStorageVolumes() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val storageManager = mContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        listOf(getUserStorage()) + storageManager.storageVolumes
            .mapNotNull { volumes ->
                StorageData(
                    name = volumes.mediaStoreVolumeName ?: mContext.getString(R.string.unknown),
                    directory = volumes.directory ?: return@mapNotNull null
                )
            }
    } else {
        val storageVolumes = mutableListOf(getUserStorage())
        File(Environment.getExternalStorageDirectory().path).takeIf {
            it.exists()
        }?.let { file ->
            StorageData(
                name = mContext.getString(R.string.internal_storage),
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