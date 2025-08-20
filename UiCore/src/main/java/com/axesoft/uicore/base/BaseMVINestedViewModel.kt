package com.axesoft.uicore.base

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseMVINestedViewModel<Event : Any, State : BaseState<Action>, Action : BaseAction>(
    nestedViewModels: @JvmSuppressWildcards Set<BaseMVINestedViewModel<*, *, *>> = setOf()
) : BaseNestedViewModel<Event>(nestedViewModels) {

    // ----State Handling----------------------------------------------------------------------------------------------------

    protected val _state: MutableStateFlow<State> by lazy { MutableStateFlow(getInitialState()) }

    val state: StateFlow<State> = _state.asStateFlow()

    protected abstract fun getInitialState(): State

    private var logSender: ((action: BaseAction) -> Unit)? = null

// ----Actions Handling--------------------------------------------------------------------------------------------------

    private val _actions = MutableSharedFlow<Action>()

    /**
     * Updates the current screen [state][State]
     * @param action A function that takes the current state and returns the new state.
     */
    protected fun updateState(action: (State) -> State) {
        _state.update {
            action(it)
        }
    }

    /**
     * This method is called each time a new action is sent to ViewModel.
     * @param action action sent from UI
     */
    abstract fun handleAction(action: Action)

    protected fun sendAction(action: Action) {
        scope.launch {
            _actions.emit(action)
        }
    }

    init {
        handleFlows(nestedViewModels)
        scope.launch {
            _actions.collectLatest { action ->
                logSender?.invoke(action)
                handleAction(action)
            }
        }
    }

    internal fun setLogSender(sender: (action: BaseAction) -> Unit) {
        logSender = sender
        nestedViewModels.forEach {
            (it as? BaseMVINestedViewModel<*, *, *>)?.setLogSender(sender)
        }
    }
}
