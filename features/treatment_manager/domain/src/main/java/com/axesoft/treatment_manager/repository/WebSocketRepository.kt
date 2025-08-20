package com.axesoft.treatment_manager.repository

interface WebSocketRepository {
    fun start(onEvent: (WebSocketEvent) -> Unit)
    fun stop()
}

sealed class WebSocketEvent {
    object Connected : WebSocketEvent()
    object Disconnected : WebSocketEvent()
    data class MessageReceived(val text: String) : WebSocketEvent()
    data class Error(val throwable: Throwable) : WebSocketEvent()
}
