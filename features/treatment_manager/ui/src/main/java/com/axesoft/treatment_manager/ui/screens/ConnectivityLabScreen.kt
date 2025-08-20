package com.axesoft.treatment_manager.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.axesoft.treatment_manager.ui.viewmodel.ConnectivityLabViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConnectivityLabScreen(
    viewModel: ConnectivityLabViewModel = hiltViewModel<ConnectivityLabViewModel>(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var granted by remember { mutableStateOf(viewModel.hasWifiPermission) }

    val permissions = remember {
        mutableListOf<String>().apply {
            add(Manifest.permission.ACCESS_WIFI_STATE)
            add(Manifest.permission.CHANGE_WIFI_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            else
                add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        granted = results.all { it.value }
        if (granted) viewModel.markPermissionGranted()
    }

    // --- Permission check + event handling ---
    LaunchedEffect(Unit) {
        if (!granted) {
            val missing = permissions.filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isNotEmpty()) {
                launcher.launch(missing.toTypedArray())
            } else {
                granted = true
                viewModel.markPermissionGranted()
            }
        }

        viewModel.events.collect { event ->
            when (event) {
                is ConnectivityLabViewModel.Event.Navigate -> {
                    Toast.makeText(context, "Navigate to ${event.route}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Connectivity Lab", style = MaterialTheme.typography.headlineMedium)
        Text("Name: ${state.doctorName}")
        Text("ID: ${state.id}")

        Spacer(Modifier.height(16.dp))

        Text(
            text = "State: ${stateDescription(state)}",
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = "Discovery Status: ${discoveryDescription(state)}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Peers found: ${state.peersCount}",
            style = MaterialTheme.typography.bodySmall
        )

        if (state.errorCode != 0) {
            Text(
                text = "Error code: ${state.errorCode}",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Wi-Fi Discovery Buttons ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { state.onAction(ConnectivityLabViewModel.Action.InitDiscovery) }
            ) {
                Text("Start Discovery")
            }

            Button(
                onClick = { state.onAction(ConnectivityLabViewModel.Action.StopDiscovery) }
            ) {
                Text("Stop Discovery")
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Loopback Section ---
        Text(
            text = "Loopback State: ${state.loopbackState}",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { state.onAction(ConnectivityLabViewModel.Action.EnableLoopback) }
            ) {
                Text("Enable Loopback")
            }

            Button(
                onClick = { state.onAction(ConnectivityLabViewModel.Action.DisableLoopback) }
            ) {
                Text("Disable Loopback")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("WebSocket: ${state.webSocketState ?: ""}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { state.onAction(ConnectivityLabViewModel.Action.StartWebSocket) }
            ) {
                Text("Start Web socket")
            }

            Button(
                onClick = { state.onAction(ConnectivityLabViewModel.Action.StopWebSocket) }
            ) {
                Text("Stop web socket")
            }
        }
    }
}

fun stateDescription(state: ConnectivityLabViewModel.ViewState): String {
    return when {
        state.isDiscovering -> "Discovering"
        state.peersCount > 0 -> "Peers Found: ${state.peersCount}"
        else -> "Idle"
    }
}

fun discoveryDescription(state: ConnectivityLabViewModel.ViewState): String {
    return when {
        state.errorCode != 0 -> "Error(${state.errorCode})"
        state.isDiscovering -> "Discovering..."
        state.peersCount > 0 -> "Peers Found (${state.peersCount})"
        state.isConnectingPeer.not() -> "Connecting to ${state.isConnectingPeer}"
        state.isConnected -> "Connected"
        state.groupLost -> "Group Lost"
        else -> "Idle"
    }
}
