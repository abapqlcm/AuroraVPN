package com.auroravpn.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
import com.whitedns.whiteaesther.data.AppSettings

/**
 * The whole Aurora surface.
 *
 * Four tabs hold the product's four standing interests, and every remaining
 * capability hangs off one of them as a detail screen. The bar hides itself on
 * a detail screen, because those have their own back affordance.
 *
 * Navigation is deliberately un-animated. A crossfade between two screens each
 * of which carries a Canvas and a flow collection is a frame of duplicated
 * work on a phone that does not have a frame to spare, and the perception the
 * user has of that is "lag". The instant swap reads as responsive instead.
 *
 * The one [AuroraViewModel] is owned here and passed down, so a screen never
 * reaches back for state and every screen survives rotation by re-collecting
 * the same flows.
 */
@Composable
fun AuroraApp(
    viewModel: AuroraViewModel,
    telemetry: AuroraTelemetry,
    onConnect: (AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onClearLog: () -> Unit,
    onShareLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: AuroraTab.startRoute
    val showBar = AuroraTab.entries.any { it.route == currentRoute }

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
                // Every primary screen is laid out inside this padding, which
                // is how the bar stops covering the last row of a list.
                contentPadding = if (showBar) {
                    PaddingValues(bottom = com.auroravpn.app.ui.design.AuroraDimensions.bottomBarClearance)
                } else {
                    PaddingValues(0.dp)
                }
            )

            if (showBar) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    AuroraBottomBar(
                        destinations = AuroraTab.entries,
                        currentRoute = currentRoute,
                        onSelect = { route ->
                            // A single top-level navigate with state saving, so a
                            // tab you have already visited is the instance you
                            // left, not a rebuild.
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
    onConnect: (AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onClearLog: () -> Unit,
    onShareLog: () -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AuroraTab.startRoute,
        modifier = modifier,
        // No enter/exit transitions. Two heavy composables crossfading at once
        // is the source of the navigation jank; an instant swap does not cost
        // a frame.
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None },
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
                contentPadding = contentPadding,
            )
        }
        composable(AuroraTab.Activity.route) {
            AuroraActivityScreen(
                viewModel = viewModel,
                telemetry = telemetry,
                onLogs = { navController.navigate(AuroraDetail.Logs.route) },
                onDiagnostics = { navController.navigate(AuroraDetail.Diagnostics.route) },
                contentPadding = contentPadding,
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
                contentPadding = contentPadding,
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
