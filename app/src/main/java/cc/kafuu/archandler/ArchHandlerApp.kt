package cc.kafuu.archandler

import android.app.Application
import android.util.Log
import cc.kafuu.archandler.libs.AppLibs
import cc.kafuu.archandler.libs.archive.ArchiveManager
import cc.kafuu.archandler.libs.manager.FileManager
import com.chibatching.kotpref.Kotpref
import net.sf.sevenzipjbinding.SevenZip
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


class ArchHandlerApp : Application() {
    companion object {
        private const val TAG = "ArchHandlerApp"
    }

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        startKoin {
            androidContext(this@ArchHandlerApp)
            modules(appModules)
        }
        testVersion()
    }

    private fun testVersion() {
        val version = SevenZip.getSevenZipVersion()
        Log.i(
            TAG,
            "7-zip version: ${version.major}.${version.minor}.${version.build} (${version.version}), ${version.date} ${version.copyright}"
        )
        Log.i(TAG, "7-Zip-JBinding version: ${SevenZip.getSevenZipJBindingVersion()}")
        Log.i(TAG, "Native library initialized: ${SevenZip.isInitializedSuccessfully()}")
    }
}

private val appModules = module {
    singleOf(::AppLibs)
    singleOf(::FileManager)
    singleOf(::ArchiveManager)
}