package cc.kafuu.archandler.feature.main.presentation

import java.io.File

sealed class LoadState {
    data object None : LoadState()

    data object ExternalStoragesLoading : LoadState()

    data object DirectoryLoading : LoadState()

    data class Pasting(
        val isMoving: Boolean,
        val src: File,
        val dest: File,
        val totality: Int,
        val quantityCompleted: Int
    ) : LoadState()
}