package com.axesoft.uicore.base

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axesoft.uicore.api.toMessage
import com.axesoft.uicore.exception.ApiException
import com.axesoft.uicore.exception.UnhandledException
import com.axesoft.uicore.navigation.ScreenResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<Event : Any, State : Any>(
    val nestedViewModels: @JvmSuppressWildcards Set<BaseNestedViewModel<*>> = setOf()
) : ViewModel(), ViewModelHandler {

    private var screenResultRegistrar: ScreenResultRegistrar? = null

    private var _navGraphDestinationId: Int? = null

    val navGraphDestinationId: Int?
        get() = _navGraphDestinationId

    // A shared flow for emitting events from this ViewModel
    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events

    // A mutable state flow for managing the state of this ViewModel
    protected abstract val _state: MutableStateFlow<State>
    val state: StateFlow<State> by lazy { _state }

    // A coroutine exception handler to catch and handle errors
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, error ->
        handleCoroutineError(error)
    }

    private val _error = MutableStateFlow("")
    internal val error: StateFlow<String> = _error

    open fun handleCoroutineError(error: Throwable) {
        handleError(error)
        Log.e(this::class.simpleName, error.message ?: error.toString())
    }

    protected open fun handleError(throwable: Throwable) {
        if (throwable is ApiException) {
            launch {
                _error.emit(throwable.toMessage())
            }
        } else {
            launch {
                _error.emit(UnhandledException().message.orEmpty())
            }
        }
    }

    internal fun clearError() {
        _error.value = ""
    }

    protected open fun handleScreenResult(screenResult: Any?, additionalInfo: Any?) {
        nestedViewModels.forEach {
            it.handleScreenResult(screenResult = screenResult, additionalInfo = additionalInfo)
        }
    }

    protected fun emitError(error: String) {
        _error.tryEmit(error)
    }

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext + coroutineExceptionHandler

    // Collect events from nested ViewModels and handle them
    init {
        nestedViewModels.forEach {
            viewModelScope.launch {
                it.events.collectLatest { event ->
                    handleNestedEvents(event)
                }
            }
        }
        nestedViewModels.forEach {
            viewModelScope.launch {
                it.error.collectLatest {
                    _error.value = it
                }
            }
        }
    }

    fun registerForScreenResult(screenResultType: ScreenResult<*>, additionalInfo: Any?) {
        screenResultRegistrar = ScreenResultRegistrar(screenResultType, additionalInfo)
    }

    fun checkForScreenResult(savedState: SavedStateHandle?) {
        screenResultRegistrar?.let {
            it.screenResultType.consumeResult(savedState).let { result ->
                handleScreenResult(screenResult = result, additionalInfo = it.additionalInfo)
            }
        }
        screenResultRegistrar = null
    }

    fun setupNavGraphDestinationId(destinationId: Int?) {
        if (_navGraphDestinationId == null) {
            _navGraphDestinationId = destinationId
        }
    }

    protected fun sendEvent(event: Event) {
        launch { _events.emit(event) }
    }

    /**
     * Retrieves a nested ViewModel of the specified type.
     *
     * @return The nested ViewModel of the specified type.
     */
    protected inline fun <reified T : Any> getViewModel(): T {
        return nestedViewModels.filterIsInstance<T>().first()
    }

    /**
     * Clears resources and calls onClear for nested ViewModels when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        nestedViewModels.forEach { it.onClear() }
    }
    private class ScreenResultRegistrar(val screenResultType: ScreenResult<*>, val additionalInfo: Any?)

    @OptIn(ExperimentalContracts::class)
    @SinceKotlin("1.3")
    protected inline fun <T> Result<T>.onFailureHandler(action: (exception: Throwable) -> Unit = {}): Result<T> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        exceptionOrNull()?.let {
            handleError(it)
            action(it)
        }
        return this
    }
}
