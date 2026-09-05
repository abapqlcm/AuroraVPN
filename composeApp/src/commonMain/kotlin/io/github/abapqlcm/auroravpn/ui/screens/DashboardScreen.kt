package io.github.abapqlcm.auroravpn.shared.ui.screens
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.abapqlcm.auroravpn.shared.ui.theme.AppPalette
import io.github.abapqlcm.auroravpn.shared.ui.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.abapqlcm.auroravpn.platform.PlatformContext
import io.github.abapqlcm.auroravpn.platform.getSettings
import io.github.abapqlcm.auroravpn.platform.getSystemUtils
import io.github.abapqlcm.auroravpn.platform.isDesktop
import io.github.abapqlcm.auroravpn.platform.isWindows
import io.github.abapqlcm.auroravpn.shared.data.IpInfo
import io.github.abapqlcm.auroravpn.shared.data.PingState
import io.github.abapqlcm.auroravpn.shared.data.PsiphonEgressRegistry
import io.github.abapqlcm.auroravpn.shared.model.AetherConfig
import io.github.abapqlcm.auroravpn.shared.model.AetherProtocol
import io.github.abapqlcm.auroravpn.shared.model.ConnectionMode
import io.github.abapqlcm.auroravpn.shared.model.PsiphonChainMode
import io.github.abapqlcm.auroravpn.shared.model.ConnectionStatus
import io.github.abapqlcm.auroravpn.shared.model.SessionTraffic
import io.github.abapqlcm.auroravpn.shared.ui.components.CountryFlag
import io.github.abapqlcm.auroravpn.shared.i18n.LocalAppStrings
import io.github.abapqlcm.auroravpn.shared.i18n.StringsFa
import io.github.abapqlcm.auroravpn.shared.util.CountryNames
import kotlinx.coroutines.launch

private val IosCardBg = AppPalette.surfaceRaised
private val IosGroupBg = AppPalette.divider
private val IosSecondaryLabel = AppPalette.textSecondary
private val IosActiveGreen = AppPalette.statusConnected
private val IosActiveBlue = AppPalette.accent
private val IosScanningAmber = AppPalette.statusScanning
private val IosErrorRed = AppPalette.statusError

@Composable
fun DashboardScreen(
    config: AetherConfig,
    connectionStatus: ConnectionStatus,
    elapsedSeconds: Long,
    sessionTraffic: SessionTraffic,
    ipInfo: IpInfo = IpInfo(),
    pingState: PingState = PingState(),
    appVersion: String = "1.0.0",
    onToggleVpn: () -> Unit,
    onForceStop: () -> Unit = {},
    onUpdateConfig: (AetherConfig) -> Unit = {},
    onUpdateProtocol: (AetherProtocol) -> Unit,
    onTogglePsiphon: (Boolean) -> Unit = {},
    onRefreshIpInfo: () -> Unit = {},
    onRefreshPing: () -> Unit = {},
    onCopy: (String) -> Unit = {},
    onOpenSettingsToZeroTrust: () -> Unit = {},
    bottomContentPadding: Dp = 0.dp,
    platformContext: PlatformContext? = null,
    onSwipeDragging: (Boolean) -> Unit = {}
) {
    var showProxyOverlay by remember { mutableStateOf(true) }
    var showAdminRequiredDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var supportDialogAuto by remember { mutableStateOf(true) }
    var showPsiphonSheet by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val uriHandler = LocalUriHandler.current
    val settings = platformContext?.let { getSettings(it) }

    LaunchedEffect(Unit) {
        if (settings != null && !settings.getBoolean("support_dialog_dismissed", false)) {
            supportDialogAuto = true
            showSupportDialog = true
        }
    }
    val systemUtils = platformContext?.let { getSystemUtils(it) }

    LaunchedEffect(connectionStatus) {
        if (connectionStatus != ConnectionStatus.RUNNING) {
            showProxyOverlay = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = this.maxWidth
        val screenHeight = this.maxHeight
        val baseScale = screenWidth.value / 411f
        val scaleFactor = if (isDesktop) (baseScale * 0.82f).coerceIn(0.65f, 0.90f) else baseScale.coerceIn(0.7f, 1.1f)
        val isCompactHeight = screenHeight < 640.dp
        val isVeryCompactHeight = screenHeight < 580.dp
        val horizontalPadding = when {
            isDesktop -> 12.dp
            screenWidth < 360.dp -> 12.dp
            else -> 16.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomContentPadding + 10.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(top = if (isDesktop) 8.dp else 36.dp),
                verticalArrangement = Arrangement.spacedBy(if (isDesktop) (10 * scaleFactor).dp else (14 * scaleFactor).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = strings.APP_TITLE,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (26 * scaleFactor).sp,
                            lineHeight = (30 * scaleFactor).sp
                        )
                        Text(
                            text = if (config.connectionMode == ConnectionMode.TUNNEL) strings.SUBTITLE_TUNNEL else strings.SUBTITLE_PROXY,
                            style = MaterialTheme.typography.bodySmall,
                            color = IosSecondaryLabel,
                            fontSize = (12 * scaleFactor).sp,
                            lineHeight = (16 * scaleFactor).sp
                        )
                        if (config.protocol == AetherProtocol.ZERO_TRUST && connectionStatus == ConnectionStatus.RUNNING && config.teamName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = IosActiveGreen, modifier = Modifier.size((14 * scaleFactor).dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = buildString {
                                        append(config.teamName)
                                        val who = config.accessEmail.ifBlank { config.accessId.ifBlank { config.accessToken.takeIf { it.isNotBlank() }?.let { "token" } } }
                                        if (!who.isNullOrBlank()) append(" • $who")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IosActiveGreen,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (12 * scaleFactor).sp
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (config.connectionMode == ConnectionMode.PROXY_ONLY && connectionStatus == ConnectionStatus.RUNNING) {
                            IconButton(
                                onClick = { showProxyOverlay = true },
                                modifier = Modifier.size((32 * scaleFactor).dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = strings.PROXY_INFO,
                                    tint = IosActiveBlue,
                                    modifier = Modifier.size((22 * scaleFactor).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width((8 * scaleFactor).dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = IosGroupBg,
                            modifier = Modifier.clickable {
                                supportDialogAuto = false
                                showSupportDialog = true
                            }
                        ) {
                            Text(
                                text = "v$appVersion",
                                modifier = Modifier.padding(horizontal = (12 * scaleFactor).dp, vertical = (6 * scaleFactor).dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IosActiveBlue,
                                fontSize = (10 * scaleFactor).sp
                            )
                        }
                    }
                }

                IosStatusHeroCard(
                    connectionStatus = connectionStatus,
                    elapsedSeconds = elapsedSeconds,
                    sessionTraffic = sessionTraffic,
                    config = config,
                    ipInfo = ipInfo,
                    pingState = pingState,
                    onRefreshIpInfo = onRefreshIpInfo,
                    onRefreshPing = onRefreshPing,
                    onCopy = onCopy,
                    hideConfigChips = isCompactHeight,
                    scaleFactor = scaleFactor
                )

                if (isWindows && (connectionStatus == ConnectionStatus.RUNNING || connectionStatus == ConnectionStatus.TUN_ACTIVE)) {
                    WindowsProxyPortsCard(
                        config = config,
                        onCopy = onCopy,
                        scaleFactor = scaleFactor
                    )
                }

                if (!isVeryCompactHeight && (connectionStatus == ConnectionStatus.ERROR || connectionStatus == ConnectionStatus.RECONNECTING)) {
                    val isReconnecting = connectionStatus == ConnectionStatus.RECONNECTING
                    val bg = if (isReconnecting) IosScanningAmber.copy(alpha = 0.12f) else IosErrorRed.copy(alpha = 0.1f)
                    val tint = if (isReconnecting) IosScanningAmber else IosErrorRed
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bg)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isReconnecting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = tint, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, null, tint = tint, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isReconnecting) {
                                    if (config.smartReconnect) "${strings.RECONNECTING_AUTO} (${config.reconnectRetryLimit} ${strings.LABEL_COUNTED} • ${config.reconnectSecs}s)" else strings.STATUS_RECONNECTING
                                } else {
                                    if (config.smartReconnect) strings.CONNECTION_FAILED_RETRY else strings.CONNECTION_FAILED_TRY
                                },
                                color = tint,
                                fontSize = (11 * scaleFactor).sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy((12 * scaleFactor).dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isWindows = remember { try { System.getProperty("os.name")?.lowercase()?.contains("win") == true } catch (_: Throwable) { false } }
                    val isAndroid = remember { !isDesktop }
                    val handleToggle: () -> Boolean = {
                        if (connectionStatus == ConnectionStatus.STOPPING) {
                            onForceStop()
                            true
                        } else if (config.protocol == AetherProtocol.ZERO_TRUST && connectionStatus == ConnectionStatus.STOPPED) {
                            if (config.zeroTrustError() != null) {
                                onOpenSettingsToZeroTrust()
                                false
                            } else {
                                onToggleVpn()
                                true
                            }
                        } else if (isWindows && config.connectionMode == ConnectionMode.TUNNEL && systemUtils?.isAdministrator() == false) {
                            showAdminRequiredDialog = true
                            false
                        } else {
                            onToggleVpn()
                            true
                        }
                    }
                    if (config.connectButtonStyle == "capsule") {
                        CapsuleConnectButton(
                            connectionStatus = connectionStatus,
                            onToggle = handleToggle,
                            onRecover = onForceStop,
                            modifier = Modifier.fillMaxWidth(),
                            scaleFactor = scaleFactor
                        )
                    } else if ((isDesktop && isWindows) || isAndroid) {
                        WindowsSwipeSwitch(
                            connectionStatus = connectionStatus,
                            onToggle = handleToggle,
                            onRecover = onForceStop,
                            onAdminCancelResetKey = if (showAdminRequiredDialog) 1 else 0,
                            modifier = Modifier.fillMaxWidth(),
                            scaleFactor = scaleFactor,
                            onDraggingChanged = onSwipeDragging,
                            isSwipeMode = config.connectButtonStyle != "capsule"
                        )
                    } else {
                        val minDim = if (screenWidth < screenHeight) screenWidth else screenHeight
                        val buttonSize = (minDim * 0.28f).coerceIn(90.dp, 140.dp)
                        IosPowerButton(
                            connectionStatus = connectionStatus,
                            onToggle = { handleToggle().let {} },
                            onRecover = onForceStop,
                            size = buttonSize
                        )
                    }
                }

                if (!isVeryCompactHeight) {
                    if (!isDesktop) {
                        val psiphonAllowed = config.protocol != AetherProtocol.ZERO_TRUST
                        val psiphonOn = config.psiphonEnabled && psiphonAllowed
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = if (psiphonOn) RoundedCornerShape(20.dp) else RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(containerColor = IosCardBg)
                        ) {
                            Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AppPalette.accentVariant), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(strings.PSIPHON_CHAIN, fontWeight = FontWeight.Bold, color = Color.White, fontSize = (13 * scaleFactor).sp)
                                        Text(if (!psiphonAllowed) strings.PSIPHON_NOT_AVAILABLE_ZT else if (config.psiphonEnabled) when (config.protocol) { AetherProtocol.MASQUE -> strings.PSIPHON_OVER_MASQUE ; AetherProtocol.WG -> strings.PSIPHON_OVER_WG ; AetherProtocol.GOOL -> strings.PSIPHON_OVER_GOOL ; AetherProtocol.ZERO_TRUST -> strings.PSIPHON_ROUTE_VIA } else strings.PSIPHON_ROUTE_VIA, color = IosSecondaryLabel, fontSize = (10 * scaleFactor).sp)
                                    }
                                }
                                Switch(
                                    checked = config.psiphonEnabled && psiphonAllowed,
                                    onCheckedChange = { onTogglePsiphon(it) },
                                    enabled = psiphonAllowed && (connectionStatus == ConnectionStatus.STOPPED || connectionStatus == ConnectionStatus.ERROR),
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosActiveGreen, checkedBorderColor = Color.Transparent, uncheckedThumbColor = Color.White, uncheckedTrackColor = AppPalette.inactiveTrack, uncheckedBorderColor = Color.Transparent, disabledCheckedTrackColor = IosActiveGreen.copy(alpha = 0.4f), disabledCheckedThumbColor = Color.White.copy(alpha = 0.9f), disabledCheckedBorderColor = Color.Transparent,                                     disabledUncheckedTrackColor = AppPalette.inactiveTrack.copy(alpha = 0.6f), disabledUncheckedThumbColor = Color.White.copy(alpha = 0.7f), disabledUncheckedBorderColor = Color.Transparent)
                                )
                            }
                                if (psiphonOn) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { showPsiphonSheet = true }.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AppPalette.accentVariant.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(strings.SHOW_MORE_PSIPHON, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = (12 * scaleFactor).sp)
                                            Text(strings.SHOW_MORE_SUBTITLE, color = IosSecondaryLabel, fontSize = (10 * scaleFactor).sp)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = IosSecondaryLabel, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (isDesktop && isWindows) {
                        IosConnectionModeSegmentedControl(
                            selectedMode = config.connectionMode,
                            onModeSelected = { onUpdateConfig(config.copy(connectionMode = it)) },
                            enabled = connectionStatus == ConnectionStatus.STOPPED || connectionStatus == ConnectionStatus.ERROR,
                            scaleFactor = scaleFactor
                        )
                    }
                    IosProtocolSegmentedControl(
                        selectedProtocol = config.protocol,
                        onProtocolSelected = onUpdateProtocol,
                        enabled = connectionStatus == ConnectionStatus.STOPPED || connectionStatus == ConnectionStatus.ERROR,
                        allowedProtocols = if (config.psiphonEnabled) setOf(AetherProtocol.MASQUE, AetherProtocol.WG, AetherProtocol.GOOL) else null,
                        scaleFactor = scaleFactor
                    )
                }
            }
        }

        val offsetY = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(showProxyOverlay) {
            if (showProxyOverlay) {
                offsetY.snapTo(0f)
            }
        }

        AnimatedVisibility(
            visible = (config.connectionMode == ConnectionMode.PROXY_ONLY || config.connectionMode == ConnectionMode.SYSTEM_PROXY) && connectionStatus == ConnectionStatus.RUNNING && showProxyOverlay && !isWindows,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 36.dp)
                .graphicsLayer { translationY = offsetY.value }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetY.value < -100f) {
                                    showProxyOverlay = false
                                } else {
                                    offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetY.snapTo((offsetY.value + dragAmount).coerceAtMost(20f))
                            }
                        }
                    )
                }
        ) {
            ProxyOverlayPill(
                host = config.socksHost,
                socksPort = config.socksPort,
                httpPort = config.httpPort,
                onHide = { showProxyOverlay = false },
                onCopy = onCopy,
                scaleFactor = scaleFactor,
                psiphonEnabled = config.psiphonEnabled,
                psiphonPort = config.psiphonSocksPort
            )
        }

        if (showAdminRequiredDialog) {
            AdminRequiredDialog(
                onRelaunch = {
                    showAdminRequiredDialog = false
                    systemUtils?.relaunchAsAdmin()
                },
                onDismiss = { showAdminRequiredDialog = false },
                scaleFactor = scaleFactor
            )
        }

        if (showSupportDialog) {
            SupportDialog(
                autoShow = supportDialogAuto,
                onJoin = {
                    settings?.putBoolean("support_dialog_dismissed", true)
                    showSupportDialog = false
                    /* lux: no telegram */ Unit
                },
                onSkip = {
                    settings?.putBoolean("support_dialog_dismissed", true)
                    showSupportDialog = false
                },
                onCancel = { showSupportDialog = false },
                scaleFactor = scaleFactor
            )
        }
        if (showPsiphonSheet) {
            PsiphonOptionsSheet(
                config = config,
                onUpdateConfig = onUpdateConfig,
                onDismiss = { showPsiphonSheet = false },
                scaleFactor = scaleFactor
            )
        }
    }
}


@Composable

private fun SupportDialog(
    autoShow: Boolean,
    onJoin: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    scaleFactor: Float
) {
    // Replaced telegram note with lux creator shimmer — keep call-site compat but show Create by @iprez
    Dialog(
        onDismissRequest = { if (autoShow) onSkip() else onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val infinite = rememberInfiniteTransition(label = "creator_shimmer")
        val shimmer by infinite.animateFloat(initialValue = -1f, targetValue = 1.4f, animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "shimmer")
        val pulse by infinite.animateFloat(initialValue = 0.97f, targetValue = 1.04f, animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = pulse, scaleY = pulse), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)), border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f))) {
                Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFD4AF37).copy(alpha = 0.08f), Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.10f)), start = Offset(shimmer*260f - 120f, 0f), end = Offset(shimmer*260f + 140f, 220f))).padding((22 * scaleFactor).dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size((56 * scaleFactor).dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFD4AF37), Color(0xFF8C6A12)))).border(2.dp, Color.White.copy(alpha = 0.22f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("@", color = Color(0xFF0A0A0B), fontWeight = FontWeight.Black, fontSize = (24 * scaleFactor).sp)
                        }
                        Spacer(modifier = Modifier.height((14 * scaleFactor).dp))
                        Text("Create by @iprez", color = Color.White, fontWeight = FontWeight.Black, fontSize = (20 * scaleFactor).sp, letterSpacing = 0.5.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height((6 * scaleFactor).dp))
                        Text("AuroraVPN · Lux Edition", color = Color(0xFFD4AF37), fontWeight = FontWeight.SemiBold, fontSize = (12 * scaleFactor).sp, letterSpacing = 1.2.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height((18 * scaleFactor).dp))
                        Button(onClick = { onSkip() }, modifier = Modifier.fillMaxWidth().height((48 * scaleFactor).dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color(0xFF0A0A0B))) {
                            Text("بزن بریم ✨", fontWeight = FontWeight.Bold, fontSize = (15 * scaleFactor).sp)
                        }
                    }
                }
            }
        }
    }
}

private fun PsiphonOptionsSheet(
    config: AetherConfig,
    onUpdateConfig: (AetherConfig) -> Unit,
    onDismiss: () -> Unit,
    scaleFactor: Float
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IosCardBg,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(strings.PSIPHON_OPTIONS_TITLE, fontWeight = FontWeight.Bold, fontSize = (18 * scaleFactor).sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when (config.protocol) {
                        AetherProtocol.MASQUE -> strings.PSIPHON_OPTIONS_SUBTITLE_MASQUE
                        else -> strings.PSIPHON_OPTIONS_SUBTITLE_WG
                    },
                    color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp
                )
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black)) { Column {
                val outerOptions = listOf("MASQUE", "WireGuard", "Gool")
                val outerValues = listOf("masque", "wg", "gool")
                val currentOuter = when (config.psiphonChainOuter) { "wg" -> "WireGuard"; "gool" -> "Gool"; else -> "MASQUE" }
                IosPickerRow(icon = Icons.Default.VpnLock, iconBg = AppPalette.statusConnected, title = strings.OUTER_PROTOCOL, value = currentOuter, options = outerOptions, onOptionSelected = { idx -> val outer = outerValues[idx]; val proto = when (outer) { "wg" -> AetherProtocol.WG; "gool" -> AetherProtocol.GOOL; else -> AetherProtocol.MASQUE }; onUpdateConfig(config.copy(psiphonChainOuter = outer, protocol = proto)) })
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(when (config.psiphonChainOuter) { "wg" -> strings.PSIPHON_SHEET_OUTER_DESC_WG ; "gool" -> strings.PSIPHON_SHEET_OUTER_DESC_GOOL ; else -> strings.PSIPHON_SHEET_OUTER_DESC_MASQUE }, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                }
                if (config.protocol == AetherProtocol.MASQUE && config.psiphonEnabled) {
                    AppDivider()
                    val orderOptions = listOf("Psiphon first", "MASQUE first", "Auto")
                    val orderValues = listOf("psiphon_first", "masque_first", "auto")
                    val currentOrder = when (config.psiphonMasqueOrder) { "masque_first" -> "MASQUE first"; "auto" -> "Auto"; else -> "Psiphon first" }
                    IosPickerRow(icon = Icons.Default.SwapHoriz, iconBg = Color(0xFF30B0C7), title = strings.MASQUE_ORDER, value = currentOrder, options = orderOptions, onOptionSelected = { idx -> onUpdateConfig(config.copy(psiphonMasqueOrder = orderValues[idx])) })
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(when (config.psiphonMasqueOrder) { "masque_first" -> strings.PSIPHON_SHEET_ORDER_DESC_MASQUE_FIRST ; "auto" -> strings.PSIPHON_SHEET_ORDER_DESC_AUTO ; else -> strings.PSIPHON_SHEET_ORDER_DESC_PSIPHON_FIRST }, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                    }
                }
                val isWgFamily = config.protocol == AetherProtocol.WG || config.protocol == AetherProtocol.GOOL
                if (!isWgFamily && config.protocol != AetherProtocol.MASQUE) {
                    AppDivider()
                    val chainModes = listOf(PsiphonChainMode.AUTO, PsiphonChainMode.FALLBACK, PsiphonChainMode.ALWAYS)
                    val chainLabels = mapOf(PsiphonChainMode.AUTO to "Auto", PsiphonChainMode.FALLBACK to "Fallback", PsiphonChainMode.ALWAYS to "Always")
                    IosPickerRow(icon = Icons.Default.Sync, iconBg = AppPalette.accent, title = strings.PSIPHON_CHAIN_MODE, value = chainLabels[config.psiphonChainMode] ?: strings.CHAIN_MODE_AUTO, options = chainModes.map { chainLabels[it]!! }, onOptionSelected = { idx -> onUpdateConfig(config.copy(psiphonChainMode = chainModes[idx])) })
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        val modeDesc = when (config.psiphonChainMode) {
                            PsiphonChainMode.AUTO -> strings.PSIPHON_SHEET_CHAIN_DESC_AUTO
                            PsiphonChainMode.FALLBACK -> strings.PSIPHON_SHEET_CHAIN_DESC_FALLBACK
                            PsiphonChainMode.ALWAYS -> strings.PSIPHON_SHEET_CHAIN_DESC_ALWAYS
                        }
                        Text(modeDesc, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(strings.PSIPHON_SHEET_WG_ALWAYS_VIA, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                    }
                }
                AppDivider()
                val availableRegions by PsiphonEgressRegistry.availableRegions.collectAsStateWithLifecycle()
                val selectedRegion = config.psiphonEgressRegion.trim().uppercase()
                val regionCodes = buildList {
                    add("")
                    addAll(availableRegions)
                    if (selectedRegion.isNotEmpty() && selectedRegion !in availableRegions) add(selectedRegion)
                }
                val regionOptions = regionCodes.map { CountryNames.label(it) }
                IosPickerRow(icon = Icons.Default.Public, iconBg = Color(0xFF30B0C7), title = strings.EXIT_COUNTRY, value = CountryNames.label(selectedRegion), options = regionOptions, onOptionSelected = { idx -> onUpdateConfig(config.copy(psiphonEgressRegion = regionCodes[idx])) })
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(strings.PSIPHON_SHEET_EXIT_AUTO, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                }
                if (isWgFamily) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(strings.PSIPHON_SHEET_EGRESS_WARN_WG, color = Color(0xFFFFCC00), fontSize = (11 * scaleFactor).sp, lineHeight = (15 * scaleFactor).sp)
                    }
                }
            } }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(strings.HOW_IT_WORKS, fontWeight = FontWeight.Bold, color = Color.White, fontSize = (14 * scaleFactor).sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(when (config.psiphonChainOuter) { "wg" -> strings.PSIPHON_SHEET_HOW_WG ; "gool" -> strings.PSIPHON_SHEET_HOW_GOOL ; else -> strings.PSIPHON_SHEET_HOW_MASQUE }, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (17 * scaleFactor).sp)
                }
            }
        }
        }
    }
}
