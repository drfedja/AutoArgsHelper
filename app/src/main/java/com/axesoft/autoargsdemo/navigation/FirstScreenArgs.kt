package com.axesoft.autoargsdemo.navigation

import com.axesoft.autoargsdestination.AutoArgsDestination
import kotlinx.serialization.Serializable

@Serializable
data class FirstScreenArgs(val userId: Int, val userName: String)

object FirstScreenDestination : AutoArgsDestination<FirstScreenArgs>(FirstScreenArgs.serializer()) {
    override val baseRoute = "firstScreen"
    override val navGraphRoute = "first_graph"
}
