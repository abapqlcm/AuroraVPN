package com.auroravpn.app.vpn

import android.app.Application
import android.content.Intent
import android.net.VpnService
import android.util.Log
import com.auroravpn.app.core.NativeAetherBridge
import com.auroravpn.app.service.AuroraVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Drives the tunnel and owns its state.
 *
 * This is the real controller. It asks Android for permission, starts
 * [AuroraVpnService], which builds the TUN descriptor, and hands the descriptor
 * to the engine. Every status the UI displays comes from the engine's own
 * return values or from the platform's callbacks — never from a timer and never
 * from a constant.
 *
 * The controller is deliberately thin. It holds the status sink and the
 * permission question; [AuroraVpnEngine] owns the engine's lifecycle, because
 * the two have different lifespans (the engine lives with the service, the
 * controller lives with the process).
 */
class VpnController(private val app: Application) {

  private val _status = MutableStateFlow<VpnStatus>(VpnStatus.Idle)
  val status: StateFlow<VpnStatus> = _status.asStateFlow()

  /**
   * The sink the engine writes into. The service owns the engine; the
   * controller owns the sink, so the UI and the engine share one status.
   */
  val statusSink: MutableStateFlow<VpnStatus>
    get() = _status

  @Volatile private var service: AuroraVpnService? = null

  val isEngineLoaded: Boolean
    get() = NativeAetherBridge.isLoaded

  val engineVersion: String?
    get() = NativeAetherBridge.versionOrNull()

  /** The engine's own log lines, drained on demand for diagnostics. */
  val engineLogs: List<String>
    get() = NativeAetherBridge.drainLog()

  /** True when [VpnService.prepare] would show the system permission dialog. */
  fun permissionRequired(): Boolean {
    return VpnService.prepare(app) != null
  }

  fun toggle() {
    when (_status.value) {
      VpnStatus.Idle, is VpnStatus.Error, VpnStatus.PermissionRequired -> connect()
      is VpnStatus.Connected -> disconnect()
      else -> {}
    }
  }

  /**
   * Starts the service, which builds the TUN and runs the engine. If the
   * permission is missing the state becomes [VpnStatus.PermissionRequired] and
   * the caller (the activity) must resolve it before calling [onPermissionResult].
   */
  fun connect() {
    if (_status.value.isBusy) return

    if (permissionRequired()) {
      _status.value = VpnStatus.PermissionRequired
      return
    }
    startService()
  }

  /** Result of the system permission dialog; called by MainActivity. */
  fun onPermissionResult(granted: Boolean) {
    if (granted) startService() else _status.value = VpnStatus.PermissionRequired
  }

  private fun startService() {
    Log.i(TAG, "CONNECT_REQUESTED")
    val intent = Intent(app, AuroraVpnService::class.java)
      .setAction(AuroraVpnService.ACTION_CONNECT)
    app.startService(intent)
  }

  fun disconnect() {
    if (_status.value.isBusy) return
    Log.i(TAG, "DISCONNECT_REQUESTED")
    service?.disconnect() ?: run {
      // The service is not bound (process was restarted). Ask it to start and
      // stop itself.
      app.startService(
        Intent(app, AuroraVpnService::class.java)
          .setAction(AuroraVpnService.ACTION_DISCONNECT)
      )
    }
  }

  /** Called by the service once it has been created and bound. */
  fun attachService(service: AuroraVpnService) {
    this.service = service
  }

  /** Called by the service when the platform revokes the session. */
  fun onRevoke() {
    _status.value = VpnStatus.Idle
  }

  companion object {
    private const val TAG = "VpnController"
  }
}
