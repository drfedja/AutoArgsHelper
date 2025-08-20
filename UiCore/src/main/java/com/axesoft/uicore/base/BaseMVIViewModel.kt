package com.axesoft.uicore.base

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseMVIViewModel <Event : Any, State : BaseState<Action>, Action: BaseAction>(
    nestedViewModels: @JvmSuppressWildcards Set<BaseMVINestedViewModel<*, *, *>> = setOf()
) : BaseViewModel<Event, State>(nestedViewModels), CoroutineScope {

    override val _state by lazy { MutableStateFlow(getInitialState()) }

    // ----State Handling----------------------------------------------------------------------------------------------------

    protected abstract fun getInitialState(): State

    protected fun updateState(action: (State) -> State) {
        _state.update {
            action(it)
        }
    }

// ----Actions Handling--------------------------------------------------------------------------------------------------

    private val _actions = MutableSharedFlow<Action>()

    abstract fun handleAction(action: Action)

    protected fun sendAction(action: Action) {
        launch {
            _actions.emit(action)
        }
    }

    init {
        handleFlows(nestedViewModels)
        nestedViewModels.forEach {
            it.setLogSender (sender = ::loggingAction)
        }
        launch {
            _actions.collectLatest { action ->
                handleAction(action)
            }
        }
    }

    private fun loggingAction(action: BaseAction) {
        val logger = action.eventLog
        if (logger is EventLogging.Logging) {
            val message = logger.description
            Log.d("MVI_logger", message)
        }
    }
}

