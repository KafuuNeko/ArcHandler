package cc.kafuu.archandler.feature.main.presentation

import android.os.Bundle
import cc.kafuu.archandler.libs.core.IViewEvent
import java.io.File

sealed class MainViewEvent : IViewEvent {
    data object JumpFilePermissionSetting : MainViewEvent()

    data class StartArchiveViewActivity(val params: Bundle) : MainViewEvent()

    data class StartDuplicateFinderActivity(val params: Bundle) : MainViewEvent()

    data class RequestInstallApk(val file: File) : MainViewEvent()
}