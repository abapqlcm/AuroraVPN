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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.auroravpn.app.vpn.VpnStatus
import com.auroravpn.app.vpn.isBusy
import com.auroravpn.app.vpn.isProtecting

/**
 * The main screen. Everything on it is derived from the VPN status, so there is
 * one source of truth and no screen-local state to get out of sync.
 */
@Composable
fun HomeScreen(
  controller: VpnController,
  onOpenSettings: () -> Unit,
  onRequestVpnPermission: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val status by controller.status.collectAsStateWithLifecycle()

  // The sky comes alive once the tunnel is up and rests while it is down.
  val skyIntensity = when {
    status.isProtecting -> 1f
    status.isBusy -> 0.75f
    status is VpnStatus.Error -> 0.5f
    else -> 0.65f
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
        status = status,
        enabled = !status.isBusy,
        onClick = {
          // The permission state is not an error the user can retry past; it
          // needs the system dialog, so it goes to the activity.
          if (status is VpnStatus.PermissionRequired) {
            onRequestVpnPermission()
          } else {
            controller.toggle()
          }
        },
      )

      // The status line. AnimatedContent so the wording does not snap; a hard
      // swap between "connecting" and "connected" looks like a glitch.
      AnimatedContent(
        targetState = status,
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

      // The error reason, when the engine gave one. Raw engine messages only —
      // a fabricated explanation would be worse than the truth.
      (status as? VpnStatus.Error)?.reason?.let { reason ->
        Text(
          text = reason,
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

private fun statusText(status: VpnStatus): String = when (status) {
  VpnStatus.Idle -> "آمادهٔ اتصال"
  VpnStatus.Preparing -> "در حال آماده‌سازی مسیر"
  is VpnStatus.Connecting -> "در حال دست‌دهی با سرور"
  is VpnStatus.Connected -> "اتصال امن برقرار است"
  VpnStatus.Disconnecting -> "در حال قطع ارتباط"
  is VpnStatus.Reconnecting -> "در حال اتصال مجدد"
  is VpnStatus.Error -> "اتصال ناموفق بود"
  VpnStatus.PermissionRequired -> "نیاز به اجازهٔ VPN"
}

@Composable
private fun statusColor(status: VpnStatus) = when {
  status.isProtecting -> MaterialTheme.colorScheme.tertiary
  status is VpnStatus.Error -> MaterialTheme.colorScheme.error
  status is VpnStatus.PermissionRequired -> MaterialTheme.colorScheme.error
  else -> MaterialTheme.colorScheme.onSurfaceVariant
}
