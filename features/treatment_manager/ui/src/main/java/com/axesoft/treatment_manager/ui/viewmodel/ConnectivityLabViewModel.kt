package com.axesoft.treatment_manager.ui.viewmodel

import com.axesoft.treatment_manager.repository.LoopbackClientState
import com.axesoft.treatment_manager.repository.LoopbackRepository
import com.axesoft.treatment_manager.repository.PermissionRepository
import com.axesoft.treatment_manager.repository.WebSocketEvent
import com.axesoft.treatment_manager.repository.WebSocketRepository
import com.axesoft.treatment_manager.repository.WifiP2pManagerWrapper
import com.axesoft.uicore.base.BaseAction
import com.axesoft.uicore.base.BaseMVIViewModel
import com.axesoft.uicore.base.BaseState
import com.axesoft.uicore.base.EventLogging
import dagger.hilt.android.lifecycle.HiltViewModel
import com.axesoft.treatment_manager.ui.destination.ConnectivityLabsDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val MAX_RETRY_DELAY = 30_000L

@HiltViewModel
class ConnectivityLabViewModel @Inject constructor(
    private val args: ConnectivityLabsDestination.ConnectivityLabArgs,
    private val wifiManager: WifiP2pManagerWrapper,
    private val loopbackRepo: LoopbackRepository,
    private val permissionRepository: PermissionRepository,
    private val webSocketRepository: WebSocketRepository
) : BaseMVIViewModel<
        ConnectivityLabViewModel.Event,
        ConnectivityLabViewModel.ViewState,
        ConnectivityLabViewModel.Action
        >()
{

    private var retryDelayMs = 1000L

    val hasWifiPermission: Boolean by lazy {
        permissionRepository.wifiPermissionGranted
    }

    fun markPermissionGranted() {
        permissionRepository.saveWifiPermission(true)
    }

    override fun getInitialState(): ViewState = ViewState(
        isDiscovering = false,
        heartbeatsSent = 0,
        heartbeatsAcked = 0,
        consecutiveMisses = 0,
        loopbackState = LoopbackClientState.DISCONNECTED,
        doctorName = args.doctorName,
        id = args.id,
        onAction = ::sendAction
    )

    override fun handleAction(action: Action) {
        when (action) {
            Action.InitDiscovery -> startWifiDiscovery()
            Action.StopDiscovery -> stopWifiDiscovery()
            Action.EnableLoopback -> observeLoopback()
            Action.StartWebSocket -> startWebSocket()
            Action.StopWebSocket -> stopWebSocket()
            Action.DisableLoopback -> loopbackRepo.disableLoopback()
        }
    }

    private fun observeLoopback() {
        loopbackRepo.enableLoopback { sent, acked, misses, state ->
            updateState {
                it.copy(
                    heartbeatsSent = sent,
                    heartbeatsAcked = acked,
                    consecutiveMisses = misses,
                    loopbackState = state
                )
            }
        }
    }

    private fun startWifiDiscovery() {
        updateState { it.copy(isDiscovering = true) }
        wifiManager.startDiscovery(
            onPeersFound = { peers ->
                updateState { it.copy(peersCount = peers.size) }
                if (peers.isEmpty()) scheduleRetry()
            },
            onError = { code ->
                updateState { it.copy(errorCode = code) }
                scheduleRetry()
            }
        )
    }

    private fun stopWifiDiscovery() {
        wifiManager.stopDiscovery()
        updateState { it.copy(isDiscovering = false) }
    }

    fun startWebSocket() {
        webSocketRepository.start { webSocketEvent ->
            updateState { it.copy(webSocketState = webSocketEvent) }
        }
    }

    fun stopWebSocket() {
        webSocketRepository.stop()
    }

    private fun scheduleRetry() {
        val jitter = (retryDelayMs * 0.2).toLong()
        val delayWithJitter = retryDelayMs + Random.nextLong(-jitter, jitter)

        launch {
            delay(delayWithJitter)
            // retry only if still discovering
            state.value.takeIf { it.isDiscovering }?.let {
                startWifiDiscovery()
            }
        }
        // double delay for next retry, cap at max
        retryDelayMs = (retryDelayMs * 2).coerceAtMost(MAX_RETRY_DELAY)
    }

    sealed class Event {
        data class Navigate(val route: String) : Event()
    }

    sealed class Action : BaseAction() {
        object InitDiscovery : Action() {
            override val eventLog: EventLogging = EventLogging.Logging("init discovery")
        }
        object StopDiscovery : Action() {
            override val eventLog: EventLogging = EventLogging.Logging("stop discovery")
        }
        object EnableLoopback : Action() {
            override val eventLog: EventLogging = EventLogging.Logging("enable loopback")
        }
        object DisableLoopback : Action() {
            override val eventLog: EventLogging = EventLogging.Logging("disable loopback")
        }
        object StartWebSocket : Action() {
            override val eventLog: EventLogging = EventLogging.Logging("start web socket")
        }
        object StopWebSocket : Action() {
            override val eventLog: EventLogging = EventLogging.Logging("stop web socket")
        }
    }

    // ---- ViewState ----
    data class ViewState(
        val isDiscovering: Boolean = false,
        val peersCount: Int = 0,
        val errorCode: Int = 0,
        val heartbeatsSent: Int = 0,
        val heartbeatsAcked: Int = 0,
        val consecutiveMisses: Int = 0,
        val isConnectingPeer: Boolean = false,
        val isConnected: Boolean = false,
        val groupLost: Boolean = false,
        val webSocketState: WebSocketEvent? = null,
        val loopbackState: LoopbackClientState = LoopbackClientState.DISCONNECTED,
        val doctorName: String,
        val id: Int,
        override val onAction: (Action) -> Unit = {}
    ) : BaseState<Action>()
}