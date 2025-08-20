package com.axesoft.treatment_manager.data.data

import com.axesoft.treatment_manager.data.data.LoopbackEndpoints.TEST_SOCKET
import com.axesoft.treatment_manager.repository.WebSocketEvent
import com.axesoft.treatment_manager.repository.WebSocketRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketRepositoryImpl @Inject constructor() : WebSocketRepository {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    override fun start(onEvent: (WebSocketEvent) -> Unit) {
        val request = Request.Builder()
            .url(TEST_SOCKET)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                onEvent(WebSocketEvent.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onEvent(WebSocketEvent.MessageReceived(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onEvent(WebSocketEvent.Disconnected)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onEvent(WebSocketEvent.Error(t))
            }
        })
    }

    override fun stop() {
        webSocket?.cancel()
        webSocket = null
    }
}

internal object LoopbackEndpoints {
    const val TEST_SOCKET = "wss://echo.websocket.events"
}
