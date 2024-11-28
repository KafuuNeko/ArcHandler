package cc.kafuu.archandler

import android.app.Application
import android.util.Log
import com.chibatching.kotpref.Kotpref
import net.sf.sevenzipjbinding.SevenZip
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
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
            modules(modules)
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

private val modules = module {

}