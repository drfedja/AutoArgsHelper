package com.axesoft.uicore.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal interface ViewModelHandler : CoroutineScope {

    /**
     * Handles an event emitted by a nested ViewModel.
     * @param event The event emitted by the nested ViewModel. The type of this event is unspecified,
     *              so it can be any object (`Any`).
     */
    fun handleNestedEvents(event: Any) {}

    /**
     * Handles a state change from a nested ViewModel.
     * @param state The new state from the nested ViewModel. The type of this state is unspecified,
     *              so it can be any object (`Any`).
     */
    fun handleNestedStateChange(state: BaseState<*>) {}
}

internal fun ViewModelHandler.handleFlows(
    nestedViewModels: @JvmSuppressWildcards Set<BaseMVINestedViewModel<*, *, *>>
) {
    nestedViewModels.forEach {
        launch {
            it.state.collectLatest {
                handleNestedStateChange(it)
            }
        }
    }
}