package com.auroravpn.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.background.AuroraSky
import com.auroravpn.app.ui.components.ConnectButton
import com.auroravpn.app.vpn.VpnController
import com.auroravpn.app.vpn.VpnState
import com.auroravpn.app.vpn.isBusy
import com.auroravpn.app.vpn.isProtecting
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

/**
 * The main screen. Everything on it is derived from the VPN state, so there is
 * one source of truth and no screen-local state to get out of sync.
 */
@Composable
fun HomeScreen(
  controller: VpnController,
  onOpenSettings: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val state by controller.state.collectAsStateWithLifecycle()
  val error by controller.errorMessage.collectAsStateWithLifecycle()

  // The sky comes alive once the tunnel is up and rests while it is down.
  val skyIntensity = when {
    state.isProtecting -> 1f
    state.isBusy -> 0.55f
    state == VpnState.ERROR -> 0.35f
    else -> 0.25f
  }

  Box(modifier = modifier.fillMaxSize()) {
    AuroraSky(
      modifier = Modifier.fillMaxSize(),
      intensity = skyIntensity,
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(contentPadding)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
      Text(
        text = "AuroraVPN",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
      )

      ConnectButton(
        state = state,
        onClick = controller::toggle,
      )

      // The status line. AnimatedContent so the wording does not snap; a hard
      // swap between "connecting" and "connected" looks like a glitch.
      AnimatedContent(
        targetState = state,
        transitionSpec = {
          (fadeIn(tween(250)) togetherWith fadeOut(tween(250)))
        },
        label = "status",
      ) { s ->
        Text(
          text = statusText(s),
          style = MaterialTheme.typography.bodyMedium,
          color = statusColor(s),
          textAlign = TextAlign.Center,
        )
      }

      if (error != null) {
        Text(
          text = error.orEmpty(),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center,
        )
      }
    }

    // Settings entry top-end. Kept out of the main column so the centre stays
    // empty and quiet — the ring is the only thing the eye should land on.
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(12.dp),
    ) {
      IconButton(onClick = onOpenSettings) {
        Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = "تنظیمات",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

private fun statusText(state: VpnState): String = when (state) {
  VpnState.IDLE -> "آمادهٔ اتصال"
  VpnState.REQUESTING -> "در حال دریافت اجازه"
  VpnState.CONNECTING -> "در حال دست‌دهی با سرور"
  VpnState.CONNECTED -> "اتصال امن برقرار است"
  VpnState.DISCONNECTING -> "در حال قطع ارتباط"
  VpnState.ERROR -> "اتصال ناموفق بود"
}

/**
 * The status line's colour. Read from the current theme by the caller so this
 * stays a plain function — MaterialTheme.colorScheme is a composition-local and
 * cannot be read outside a @Composable.
 */
@Composable
private fun statusColor(state: VpnState) = when {
  state.isProtecting -> MaterialTheme.colorScheme.tertiary
  state == VpnState.ERROR -> MaterialTheme.colorScheme.error
  else -> MaterialTheme.colorScheme.onSurfaceVariant
}
