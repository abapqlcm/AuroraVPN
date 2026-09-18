package com.auroravpn.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auroravpn.app.ui.screens.AuroraCarrierScreen
import com.auroravpn.app.ui.screens.AuroraDiagnosticsScreen
import com.auroravpn.app.ui.screens.AuroraEndpointsScreen
import com.auroravpn.app.ui.screens.AuroraHomeScreen
import com.auroravpn.app.ui.screens.AuroraIdentityScreen
import com.auroravpn.app.ui.screens.AuroraLogsScreen
import com.auroravpn.app.ui.screens.AuroraMoreScreen
import com.auroravpn.app.ui.screens.AuroraRoutesScreen
import com.auroravpn.app.ui.screens.AuroraSettingsScreen
import com.auroravpn.app.ui.screens.AuroraTransportScreen

/**
 * The whole Aurora surface: a bottom bar of five destinations and a stack of advanced
 * screens behind "More".
 *
 * The activity owns the one [AuroraViewModel] and passes it down, so a screen never has
 * to reach back for state and every screen survives rotation by re-collecting the same
 * flows.
 */
@Composable
fun AuroraApp(
    viewModel: AuroraViewModel,
    onConnect: (com.whitedns.whiteaesther.data.AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onClearLog: () -> Unit,
    onShareLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: AuroraDestination.startRoute

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val showBottomBar = AuroraDestination.entries.any { it.route == currentRoute }
            if (showBottomBar) {
                NavigationBar {
                    AuroraDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            icon = { Text(destination.label.first().toString()) },
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(AuroraDestination.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        AuroraNavHost(
            navController = navController,
            viewModel = viewModel,
            onConnect = onConnect,
            onDisconnect = onDisconnect,
            onClearLog = onClearLog,
            onShareLog = onShareLog,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun AuroraNavHost(
    navController: NavHostController,
    viewModel: AuroraViewModel,
    onConnect: (com.whitedns.whiteaesther.data.AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onClearLog: () -> Unit,
    onShareLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AuroraDestination.startRoute,
        modifier = modifier,
    ) {
        composable(AuroraDestination.Home.route) {
            AuroraHomeScreen(
                viewModel = viewModel,
                onConnect = onConnect,
                onDisconnect = onDisconnect,
            )
        }
        composable(AuroraDestination.Routes.route) {
            AuroraRoutesScreen(viewModel = viewModel)
        }
        composable(AuroraDestination.Endpoints.route) {
            AuroraEndpointsScreen(viewModel = viewModel)
        }
        composable(AuroraDestination.Transport.route) {
            AuroraTransportScreen(viewModel = viewModel)
        }
        composable(AuroraDestination.More.route) {
            AuroraMoreScreen(onNavigate = { navController.navigate(it) })
        }
        composable(AuroraAdvancedDestination.Identity.route) {
            AuroraIdentityScreen(viewModel = viewModel)
        }
        composable(AuroraAdvancedDestination.Diagnostics.route) {
            AuroraDiagnosticsScreen(viewModel = viewModel)
        }
        composable(AuroraAdvancedDestination.Logs.route) {
            AuroraLogsScreen(
                viewModel = viewModel,
                onClear = onClearLog,
                onShare = onShareLog,
            )
        }
        composable(AuroraAdvancedDestination.Settings.route) {
            AuroraSettingsScreen(viewModel = viewModel)
        }
        composable(AuroraAdvancedDestination.Carrier.route) {
            AuroraCarrierScreen(viewModel = viewModel)
        }
    }
}
