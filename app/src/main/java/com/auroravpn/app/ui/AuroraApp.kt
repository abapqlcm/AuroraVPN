package com.auroravpn.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.auroravpn.app.ui.screens.HomeScreen
import com.auroravpn.app.ui.screens.SettingsScreen
import com.auroravpn.app.vpn.VpnController

private object Routes {
  const val HOME = "home"
  const val SETTINGS = "settings"
}

/**
 * The app's navigation graph. Two destinations, no deep links, no arguments —
 * deliberately small. The VPN controller is created once in MainActivity and
 * passed by reference so the whole app sees one tunnel and one status.
 */
@Composable
fun AuroraApp(
  controller: VpnController,
  onRequestVpnPermission: () -> Unit,
) {
  val navController = rememberNavController()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.Transparent,
  ) { padding ->
    NavHost(
      navController = navController,
      startDestination = Routes.HOME,
      modifier = Modifier.fillMaxSize(),
      enterTransition = {
        slideInHorizontally(tween(280)) { it } + fadeIn(tween(280))
      },
      exitTransition = {
        slideOutHorizontally(tween(280)) { -it } + fadeOut(tween(280))
      },
      popEnterTransition = {
        slideInHorizontally(tween(280)) { -it } + fadeIn(tween(280))
      },
      popExitTransition = {
        slideOutHorizontally(tween(280)) { it } + fadeOut(tween(280))
      },
    ) {
      composable(Routes.HOME) {
        HomeScreen(
          controller = controller,
          onOpenSettings = { navController.navigate(Routes.SETTINGS) },
          onRequestVpnPermission = onRequestVpnPermission,
          contentPadding = padding,
        )
      }
      composable(Routes.SETTINGS) {
        SettingsScreen(
          contentPadding = padding,
        )
      }
    }
  }
}
