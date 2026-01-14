package cc.kafuu.archandler.feature.duplicatefinder.presentation

import cc.kafuu.archandler.libs.core.IViewEvent

sealed class DuplicateFinderViewEvent : IViewEvent {
    data object Finish : DuplicateFinderViewEvent()
}
