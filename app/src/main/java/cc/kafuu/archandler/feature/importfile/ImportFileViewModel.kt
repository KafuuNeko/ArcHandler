package cc.kafuu.archandler.feature.importfile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import cc.kafuu.archandler.R
import cc.kafuu.archandler.feature.importfile.presention.ImportFileDialogState
import cc.kafuu.archandler.feature.importfile.presention.ImportFileUiIntent
import cc.kafuu.archandler.feature.importfile.presention.ImportFileUiState
import cc.kafuu.archandler.feature.main.MainActivity
import cc.kafuu.archandler.libs.AppModel
import cc.kafuu.archandler.libs.core.AppViewEvent
import cc.kafuu.archandler.libs.core.CoreViewModelWithEvent
import cc.kafuu.archandler.libs.core.UiIntentObserver
import cc.kafuu.archandler.libs.extensions.createUniqueDirectory
import cc.kafuu.archandler.libs.extensions.generateUniqueFile
import cc.kafuu.archandler.libs.extensions.saveFile
import cc.kafuu.archandler.libs.manager.FileManager
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportFileViewModel :
    CoreViewModelWithEvent<ImportFileUiIntent, ImportFileUiState>(ImportFileUiState.None),
    KoinComponent {
    private val mFileManager by inject<FileManager>()

    @UiIntentObserver(ImportFileUiIntent.Init::class)
    private suspend fun onInit(intent: ImportFileUiIntent.Init) {
        if (!isStateOf<ImportFileUiState.None>()) return
        ImportFileUiState.Normal().run {
            setup()
            doImport(intent.uris)
        }
        ImportFileUiState.Finished.setup()
    }

    private suspend fun ImportFileUiState.Normal.doImport(uris: List<Pair<String, Uri>>) {
        copy(dialogState = ImportFileDialogState.Importing()).setup()
        val allowImport = ImportFileDialogState.ImportConfirm(count = uris.size).run {
            copy(dialogState = this).setup()
            deferredResult.awaitCompleted()
        }
        if (!allowImport) return
        val importDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        val importDirectory = File(mFileManager.getUserStorage().directory, "imported")
        val outputDirectory = File(importDirectory, importDate).createUniqueDirectory() ?: return
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) return
        uris.forEach {
            copy(dialogState = ImportFileDialogState.Importing(name = it.first)).setup()
            val outputFile = File(outputDirectory, it.first).generateUniqueFile(outputDirectory)
            get<Context>().saveFile(it.second, outputFile)
        }
        setup()
        AppViewEvent.PopupToastMessageByResId(R.string.file_imported_successfully).emit()
        AppViewEvent.StartActivity(MainActivity::class.java, Bundle().apply {
            putString(AppModel.KEY_USER_REDIRECT_PATH, outputDirectory.path)
        }).emit()
    }
}