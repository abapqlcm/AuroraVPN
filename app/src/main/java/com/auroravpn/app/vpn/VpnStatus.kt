package com.auroravpn.app.vpn

/**
 * The tunnel's lifecycle exactly as the engine can actually be in.
 *
 * This is deliberately not a copy of any reference app's states. It is the set
 * of situations [AuroraVpnService] can genuinely observe from the Aether
 * bridge: the JNI calls return Ok/Err, the VpnService permission can be
 * refused, and [android.net.VpnService] can revoke an established session.
 *
 * The UI is a pure function of this value. Nothing else holds tunnel state.
 */
sealed interface VpnStatus {

  /** Tunnel is down and idle. Nothing is running. */
  data object Idle : VpnStatus

  /**
   * The user asked to connect. Either we are waiting on the Android VPN
   * permission, or the route is being prepared. Distinct from Connecting
   * because the engine has not started yet — there is nothing to stop.
   */
  data object Preparing : VpnStatus

  /**
   * The engine is running and handshaking with an endpoint. The duration is
   * unknown to us, so any progress indicator must be indeterminate.
   */
  data class Connecting(val attempt: Int) : VpnStatus

  /** Tunnel is up and traffic is flowing. */
  data class Connected(
    /** Human-readable endpoint description, if the engine reported one. */
    val endpoint: String?,
    /** Transport in use, if known: "masque" or "wireguard". */
    val transport: String?,
    /** True when the active endpoint is a fallback rather than the primary. */
    val usingFallback: Boolean,
  ) : VpnStatus

  /** The user asked to disconnect; the tunnel is being torn down. */
  data object Disconnecting : VpnStatus

  /**
   * An established session was lost and the engine is finding another path
   * without being asked. The user did not request this.
   *
   * NOTE: Aether does not actually signal this to the UI. The state is kept on
   * the model because the engine has internal quick-reconnect, but nothing sets
   * it today — the UI must not present reconnection as a fact until a real
   * signal exists. See AUDIT.md PART 7.
   */
  data class Reconnecting(val attempt: Int) : VpnStatus

  /** Something failed. [reason] is the engine's own message, translated to a */
  data class Error(val reason: String) : VpnStatus

  /** Android refused to grant VpnService permission. Nothing else can happen. */
  data object PermissionRequired : VpnStatus
}

/** True whenever the app is between two steady states. */
val VpnStatus.isBusy: Boolean
  get() = when (this) {
    is VpnStatus.Connecting,
    is VpnStatus.Reconnecting,
    VpnStatus.Preparing,
    VpnStatus.Disconnecting -> true
    else -> false
  }

/** True whenever traffic is actually protected. */
val VpnStatus.isProtecting: Boolean
  get() = this is VpnStatus.Connected

/** True whenever the tunnel is up or trying to come up. */
val VpnStatus.isActive: Boolean
  get() = isProtecting || isBusy
