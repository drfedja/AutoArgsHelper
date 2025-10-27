package com.axesoft.uicore

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.axesoft.uicore.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter


val LocalNavController = staticCompositionLocalOf<NavController> { error("No AppState provided") }

@SuppressLint("RestrictedApi")
@Composable
fun <Event : Any, State : Any>Screen(
    viewModel: BaseViewModel<Event, State>,
    eventsHandler: NavController.(Event) -> Unit,
    content: @Composable (State) -> Unit
) {
    val viewState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val errorDialogText by viewModel.error.collectAsState()

    val navController = LocalNavController.current
    viewModel.setupNavGraphDestinationId(navController.currentBackStackEntry?.destination?.id)

    // Handling result from modal bottom sheet
    LaunchedEffect(context) {
        navController.currentBackStackEntryFlow
            .filter {
                it.maxLifecycle == Lifecycle.State.RESUMED &&
                        isDestinationTheSame(navController, viewModel)
            }
            .collect { backStack ->
                viewModel.checkForScreenResult(backStack.savedStateHandle)
            }
    }
    // Handling result from other screen
    LaunchedEffect(context) {
        if (isDestinationTheSame(navController, viewModel)) {
            viewModel.checkForScreenResult(navController.currentBackStackEntry?.savedStateHandle)
        }
    }

    LaunchedEffect(context) {
        with(navController) {
            viewModel.events.collectLatest {
                eventsHandler.invoke(this, it)
            }
        }
    }

    content(viewState)
    if (errorDialogText.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                Text(
                    modifier = Modifier.clickable {
                        viewModel.clearError()
                    },
                    text = stringResource(id = R.string.ok)
                )
            },
            title = { Text(text = stringResource(id = R.string.unexpected_error)) },
            text = { Text(text = errorDialogText) }
        )
    }
}

private fun <Event : Any, State : Any> isDestinationTheSame(
    homeNavController: NavController,
    viewModel: BaseViewModel<Event, State>
) = homeNavController.currentBackStackEntry?.destination?.id == viewModel.navGraphDestinationId
