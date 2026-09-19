package com.auroravpn.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auroravpn.app.ui.design.AuroraTheme
import com.auroravpn.app.ui.home.NetworkOrbitHomeScreen
import com.auroravpn.app.ui.navigation.AuroraBottomBar
import com.auroravpn.app.ui.navigation.AuroraDetail
import com.auroravpn.app.ui.navigation.AuroraTab
import com.auroravpn.app.ui.screens.AuroraActivityScreen
import com.auroravpn.app.ui.screens.AuroraAdvancedScreen
import com.auroravpn.app.ui.screens.AuroraCarrierScreen
import com.auroravpn.app.ui.screens.AuroraChainScreen
import com.auroravpn.app.ui.screens.AuroraDiagnosticsScreen
import com.auroravpn.app.ui.screens.AuroraEndpointsScreen
import com.auroravpn.app.ui.screens.AuroraIdentityScreen
import com.auroravpn.app.ui.screens.AuroraLogsScreen
import com.auroravpn.app.ui.screens.AuroraRoutesScreen
import com.auroravpn.app.ui.screens.AuroraSettingsScreen
import com.auroravpn.app.ui.screens.AuroraTransportScreen

/**
 * The whole Aurora surface.
 *
 * Four tabs hold the product's four standing interests, and every remaining
 * capability hangs off one of them as a detail screen. The bar hides itself on
 * a detail screen, because those have their own back affordance, and the tabs
 * are for moving between standing interests rather than for climbing a stack.
 *
 * The activity owns the one [AuroraViewModel] and passes it down, so a screen
 * never has to reach back for state and every screen survives rotation by
 * re-collecting the same flows.
 */
@Composable
fun AuroraApp(
    viewModel: AuroraViewModel,
    telemetry: AuroraTelemetry,
    onConnect: (com.whitedns.whiteaesther.data.AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onClearLog: () -> Unit,
    onShareLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: AuroraTab.startRoute

    AuroraTheme {
        Box(modifier = modifier.fillMaxSize()) {
            AuroraNavHost(
                navController = navController,
                viewModel = viewModel,
                telemetry = telemetry,
                onConnect = onConnect,
                onDisconnect = onDisconnect,
                onClearLog = onClearLog,
                onShareLog = onShareLog,
                onBack = { navController.popBackStack() },
            )

            val showBar = AuroraTab.entries.any { it.route == currentRoute }
            if (showBar) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.BottomCenter,
                ) {
                    AuroraBottomBar(
                        destinations = AuroraTab.entries,
                        currentRoute = currentRoute,
                        onSelect = { route ->
                            navController.navigate(route) {
                                popUpTo(AuroraTab.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AuroraNavHost(
    navController: NavHostController,
    viewModel: AuroraViewModel,
    telemetry: AuroraTelemetry,
    onConnect: (com.whitedns.whiteaesther.data.AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onClearLog: () -> Unit,
    onShareLog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AuroraTab.startRoute,
        modifier = modifier,
    ) {
        composable(AuroraTab.Home.route) {
            NetworkOrbitHomeScreen(
                viewModel = viewModel,
                telemetry = telemetry,
                onConnect = onConnect,
                onDisconnect = onDisconnect,
                onSettings = { navController.navigate(AuroraTab.Settings.route) },
            )
        }
        composable(AuroraTab.Routes.route) {
            AuroraRoutesScreen(
                viewModel = viewModel,
                onEndpoints = { navController.navigate(AuroraDetail.Endpoints.route) },
                onTransport = { navController.navigate(AuroraDetail.Transport.route) },
                onIdentity = { navController.navigate(AuroraDetail.Identity.route) },
            )
        }
        composable(AuroraTab.Activity.route) {
            AuroraActivityScreen(
                viewModel = viewModel,
                telemetry = telemetry,
                onLogs = { navController.navigate(AuroraDetail.Logs.route) },
                onDiagnostics = { navController.navigate(AuroraDetail.Diagnostics.route) },
            )
        }
        composable(AuroraTab.Settings.route) {
            AuroraSettingsScreen(
                viewModel = viewModel,
                onIdentity = { navController.navigate(AuroraDetail.Identity.route) },
                onDiagnostics = { navController.navigate(AuroraDetail.Diagnostics.route) },
                onLogs = { navController.navigate(AuroraDetail.Logs.route) },
                onCarrier = { navController.navigate(AuroraDetail.Carrier.route) },
                onChain = { navController.navigate(AuroraDetail.Chain.route) },
                onAdvanced = { navController.navigate(AuroraDetail.Advanced.route) },
            )
        }
        composable(AuroraDetail.Endpoints.route) {
            AuroraEndpointsScreen(viewModel = viewModel, onBack = onBack)
        }
        composable(AuroraDetail.Transport.route) {
            AuroraTransportScreen(viewModel = viewModel, onBack = onBack)
        }
        composable(AuroraDetail.Identity.route) {
            AuroraIdentityScreen(viewModel = viewModel, onBack = onBack)
        }
        composable(AuroraDetail.Diagnostics.route) {
            AuroraDiagnosticsScreen(viewModel = viewModel, onBack = onBack)
        }
        composable(AuroraDetail.Logs.route) {
            AuroraLogsScreen(
                viewModel = viewModel,
                onClear = onClearLog,
                onShare = onShareLog,
                onBack = onBack,
            )
        }
        composable(AuroraDetail.Carrier.route) {
            AuroraCarrierScreen(viewModel = viewModel, onBack = onBack)
        }
        composable(AuroraDetail.Chain.route) {
            AuroraChainScreen(viewModel = viewModel, onBack = onBack)
        }
        composable(AuroraDetail.Advanced.route) {
            AuroraAdvancedScreen(viewModel = viewModel, onBack = onBack)
        }
    }
}
