package cc.kafuu.archandler.feature.duplicatefinder.presentation

import java.io.File

sealed class DuplicateFinderUiIntent {
    data object Init : DuplicateFinderUiIntent()

    data object Back : DuplicateFinderUiIntent()

    data object StartSearch : DuplicateFinderUiIntent()

    data object CancelSearch : DuplicateFinderUiIntent()

    data object ToggleSelection : DuplicateFinderUiIntent()

    data class FileSelect(
        val file: File,
        val selected: Boolean
    ) : DuplicateFinderUiIntent()

    data class SelectAllInGroup(
        val hash: String
    ) : DuplicateFinderUiIntent()

    data object DeselectAll : DuplicateFinderUiIntent()

    data object DeleteSelected : DuplicateFinderUiIntent()
}
