package com.axesoft.treatment_manager.repository

interface WifiP2pManagerWrapper {
    fun startDiscovery(
        onPeersFound: (List<WifiPeer>) -> Unit,
        onError: (Int) -> Unit
    )
    fun stopDiscovery()
}

data class WifiPeer(
    val deviceName: String,
    val deviceAddress: String
)
