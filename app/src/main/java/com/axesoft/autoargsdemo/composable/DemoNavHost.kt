package com.axesoft.autoargsdemo.composable

import com.axesoft.treatment_manager.ui.viewmodel.ConnectivityLabViewModel
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.axesoft.treatment_manager.ui.screens.ConnectivityLabScreen
import com.axesoft.treatment_manager.ui.destination.ConnectivityLabsDestination
import com.axesoft.autoargsdemo.navigation.FirstScreenArgs
import com.axesoft.autoargsdemo.navigation.FirstScreenDestination

@Composable
fun DemoNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FirstScreenDestination.buildRoute(
            FirstScreenArgs(userId = 123, userName = "")
        )
    ) {
        composable(
            route = FirstScreenDestination.route,
            arguments = FirstScreenDestination.arguments
        ) { backStackEntry ->
            val args = FirstScreenDestination.getArguments(backStackEntry.savedStateHandle)
            FirstScreen(args) { enteredName ->
                val secondArgs =
                    ConnectivityLabsDestination.ConnectivityLabArgs(enteredName, args.userId)
                navController.navigate(
                    ConnectivityLabsDestination.buildRoute(secondArgs)
                )
            }
        }

        composable(
            route = ConnectivityLabsDestination.route,
            arguments = ConnectivityLabsDestination.arguments
        ) { backStackEntry ->
            val viewModel: ConnectivityLabViewModel = hiltViewModel(backStackEntry)
            ConnectivityLabScreen(viewModel)
        }
    }
}

