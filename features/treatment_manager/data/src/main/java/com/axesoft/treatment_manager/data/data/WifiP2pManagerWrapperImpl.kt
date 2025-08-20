package com.axesoft.treatment_manager.data.data

import android.Manifest
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.axesoft.treatment_manager.repository.WifiP2pManagerWrapper
import com.axesoft.treatment_manager.repository.WifiPeer
import javax.inject.Inject

internal class WifiP2pManagerWrapperImpl @Inject constructor(
    context: Context
) : WifiP2pManagerWrapper {

    private val wifiManager: WifiP2pManager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

    private val channel: WifiP2pManager.Channel =
        wifiManager.initialize(context, Looper.getMainLooper(), null)

    private var peerListener: WifiP2pManager.PeerListListener? = null
    private var errorListener: ((Int) -> Unit)? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun startDiscovery(
        onPeersFound: (List<WifiPeer>) -> Unit,
        onError: (Int) -> Unit
    ) {
        peerListener = WifiP2pManager.PeerListListener { peers ->
            onPeersFound(peers.deviceList.map { WifiPeer(it.deviceName, it.deviceAddress) })
        }
        errorListener = onError

        wifiManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // discovery started successfully
                Log.d("peers", "discovered")
            }

            override fun onFailure(reason: Int) {
                errorListener?.invoke(reason)
            }
        })
    }

    override fun stopDiscovery() {
        wifiManager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {
                errorListener?.invoke(reason)
            }
        })
    }
}

