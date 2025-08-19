package com.example.autoargsdemo.composable

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autoargsdemo.navigation.FirstScreenArgs
import com.example.autoargsdemo.navigation.FirstScreenDestination
import com.example.autoargsdemo.navigation.SecondScreenArgs
import com.example.autoargsdemo.navigation.SecondScreenDestination

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
                val secondArgs = SecondScreenArgs(args.userId, enteredName)
                navController.navigate(
                    SecondScreenDestination.buildRoute(secondArgs)
                )
            }
        }

        composable(
            route = SecondScreenDestination.route,
            arguments = SecondScreenDestination.arguments
        ) { backStackEntry ->
            val args = SecondScreenDestination.getArguments(backStackEntry.savedStateHandle)
            SecondScreen(args)
        }
    }
}

