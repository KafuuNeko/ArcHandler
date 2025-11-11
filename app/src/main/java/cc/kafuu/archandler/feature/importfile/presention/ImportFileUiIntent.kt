package cc.kafuu.archandler.feature.importfile.presention

import android.net.Uri

sealed class ImportFileUiIntent {
    data class Init(val uris: List<Pair<String, Uri>>) : ImportFileUiIntent()
}