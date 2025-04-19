package cc.kafuu.archandler.feature.main.presentation

import java.io.File

sealed class MainLoadState {
    data object None : MainLoadState()

    data object ExternalStoragesLoading : MainLoadState()

    data object DirectoryLoading : MainLoadState()

    data class Pasting(
        val isMoving: Boolean,
        val src: File,
        val dest: File,
        val totality: Int,
        val quantityCompleted: Int
    ) : MainLoadState()
}