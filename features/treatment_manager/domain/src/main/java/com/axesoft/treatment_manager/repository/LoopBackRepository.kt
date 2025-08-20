package com.axesoft.treatment_manager.repository

interface LoopbackRepository {
    fun enableLoopback(
        onHeartbeat: (sent: Int, acked: Int, misses: Int, state: LoopbackClientState) -> Unit
    )
    fun disableLoopback()
}

enum class LoopbackClientState {
    TRYING,
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}