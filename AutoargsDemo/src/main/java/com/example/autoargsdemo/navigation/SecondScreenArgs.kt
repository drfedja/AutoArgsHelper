package com.example.autoargsdemo.navigation

import com.axesoft.autoargsdestination.AutoArgsDestination
import kotlinx.serialization.Serializable

@Serializable
data class SecondScreenArgs(
    val userId: Int,
    val userName: String
)

object SecondScreenDestination : AutoArgsDestination<SecondScreenArgs>(SecondScreenArgs.serializer()) {
    override val baseRoute = "second"
    override val navGraphRoute = "second_graph"
}
