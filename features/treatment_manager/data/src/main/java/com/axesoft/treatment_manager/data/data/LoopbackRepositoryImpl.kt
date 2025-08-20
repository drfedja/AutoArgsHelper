package com.axesoft.treatment_manager.data.data

import com.axesoft.treatment_manager.repository.LoopbackClientState
import com.axesoft.treatment_manager.repository.LoopbackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

internal class LoopbackRepositoryImpl @Inject constructor() : LoopbackRepository {

    private var heartbeatJob: Job? = null
    private val heartbeatIntervalMs = 1500L
    private val maxMisses = 8
    private var ackThreshold = Random.nextInt(1, 5)
    private var callback: ((Int, Int, Int, LoopbackClientState) -> Unit)? = null

    override fun enableLoopback(
        onHeartbeat: (sent: Int, acked: Int, misses: Int, state: LoopbackClientState) -> Unit
    ) {
        if (heartbeatJob != null) return

        callback = onHeartbeat
        var currentState = LoopbackClientState.TRYING
        var sent = 0
        var acked = 0
        var misses = 0

        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                sent++
                val isAcked = sent > ackThreshold
                if (isAcked) {
                    acked++
                    misses = 0
                    currentState = when (currentState) {
                        LoopbackClientState.TRYING -> {
                            LoopbackClientState.CONNECTING
                        }
                        LoopbackClientState.CONNECTING -> {
                            LoopbackClientState.CONNECTED
                        }
                        LoopbackClientState.CONNECTED -> {
                            LoopbackClientState.CONNECTED
                        }
                        LoopbackClientState.DISCONNECTED -> {
                            LoopbackClientState.DISCONNECTED
                        }
                    }
                } else {
                    misses++
                    if (misses >= maxMisses) {
                        currentState = LoopbackClientState.DISCONNECTED
                        delay(1000)
                        misses = 0
                        currentState = LoopbackClientState.CONNECTING
                    }
                }

                onHeartbeat(sent, acked, misses, currentState)

                delay(heartbeatIntervalMs)
            }
        }
    }

    override fun disableLoopback() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        callback?.invoke(0, 0, 0, LoopbackClientState.DISCONNECTED)
        callback = null
    }
}

