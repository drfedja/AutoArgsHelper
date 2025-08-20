package com.axesoft.uicore.base

import android.util.Log
import com.axesoft.uicore.api.toMessage
import com.axesoft.uicore.exception.ApiException
import com.axesoft.uicore.exception.UnhandledException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

abstract class BaseNestedViewModel<Event : Any>(
    val nestedViewModels: @JvmSuppressWildcards Set<BaseNestedViewModel<*>> = setOf()
) : ViewModelHandler {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, error ->
        handleCoroutineError(error)
    }

    protected val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate + coroutineExceptionHandler
    )

    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext + coroutineExceptionHandler

    // A shared flow for propagating events to upper ViewModel
    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events

    // A shared flow to propagate error message to upper view model
    private val _error = MutableSharedFlow<String>()
    internal val error: SharedFlow<String> = _error

    init {
        // Collect events from nested ViewModels and handle them
        nestedViewModels.forEach {
            scope.launch {
                it.events.collectLatest { event ->
                    handleNestedEvents(event)
                }
            }
        }
        nestedViewModels.forEach {
            scope.launch {
                it.error.collectLatest {
                    _error.emit(it)
                }
            }
        }
    }

    open fun handleScreenResult(screenResult: Any?, additionalInfo: Any?) {
        nestedViewModels.forEach { it.handleScreenResult(screenResult = screenResult, additionalInfo = additionalInfo) }
    }

    /**
     * Handles coroutine errors by logging them.
     * Can be overridden to provide custom error handling.
     *
     * @param error Throwable representing the error.
     */
    open fun handleCoroutineError(error: Throwable) {
        handleError(error)
        Log.e(this::class.simpleName, error.message ?: error.toString())
    }

    /**
     * Sends an event to the event flow.
     *
     * @param event The event to send.
     */
    protected fun sendEvent(event: Event) {
        scope.launch { _events.emit(event) }
    }

    /**
     * Sends an error to the error flow.
     *
     * @param error The error to send.
     */
    protected fun emitError(error: String) {
        _error.tryEmit(error)
    }

    /**
     * Retrieves a nested ViewModel of the specified type.
     *
     * @return The nested ViewModel of the specified type.
     */
    protected inline fun <reified T : Any> getViewModel(): T {
        return nestedViewModels.filterIsInstance<T>().first()
    }

    protected open fun handleError(throwable: Throwable) {
        if (throwable is ApiException) {
            scope.launch {
                _error.emit(throwable.toMessage())
            }
        } else {
            scope.launch {
                _error.emit(UnhandledException().message.orEmpty())
            }
        }
    }

    /**
     * Clears resources and calls onClear for nested ViewModels when this nested ViewModel is cleared.
     */
    open fun onClear() {
        scope.cancel()
        nestedViewModels.forEach { it.onClear() }
    }

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
