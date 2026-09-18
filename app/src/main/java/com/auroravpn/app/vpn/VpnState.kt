package com.auroravpn.app.vpn

/**
 * The tunnel's lifecycle as the UI sees it.
 *
 * The UI is a pure function of this value — nothing else. Phase 4 swaps the
 * simulated [VpnController] for one that drives the real engine, and the
 * screens do not change.
 */
enum class VpnState {
  /** Tunnel is down and nothing is happening. */
  IDLE,

  /** The user asked to connect; permission or engine setup may still be pending. */
  REQUESTING,

  /** The engine is handshaking with the endpoint. This is the indeterminate part. */
  CONNECTING,

  /** Tunnel is up and traffic is flowing. */
  CONNECTED,

  /** The user asked to disconnect; the tunnel is being torn down. */
  DISCONNECTING,

  /** Something failed. [VpnController.errorMessage] holds the reason. */
  ERROR,
}

/** True whenever the app is between two steady states. */
val VpnState.isBusy: Boolean
  get() = this == VpnState.REQUESTING || this == VpnState.CONNECTING || this == VpnState.DISCONNECTING

/** True whenever traffic is actually protected. */
val VpnState.isProtecting: Boolean
  get() = this == VpnState.CONNECTED
