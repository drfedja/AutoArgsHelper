package com.axesoft.treatment_manager.ui.destination

import com.axesoft.autoargsdestination.AutoArgsDestination
import kotlinx.serialization.Serializable

object ConnectivityLabsDestination :
    AutoArgsDestination<ConnectivityLabsDestination.ConnectivityLabArgs>(
        ConnectivityLabArgs.serializer()
    ) {
    override val baseRoute = "connectivityLab"
    override val navGraphRoute = "main_graph"

    @Serializable
    data class ConnectivityLabArgs(
        val doctorName: String,
        val id: Int
    )
}
