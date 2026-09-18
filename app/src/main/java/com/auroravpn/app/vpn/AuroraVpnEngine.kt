package com.auroravpn.app.vpn

import android.content.Context
import android.net.VpnService
import android.util.Log
import com.auroravpn.app.core.NativeAetherBridge
import com.auroravpn.app.core.NativeEngineListener
import com.auroravpn.app.core.NativeSocketProtector
import com.auroravpn.app.core.PreparedEngine
import com.auroravpn.app.service.AuroraVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The single owner of the engine's lifecycle and of the tunnel state.
 *
 * Every status the UI displays comes from the engine's own return values or
 * from the platform's callbacks — never from a timer and never from a constant.
 *
 * Threading is the whole shape of this class:
 * - [nativePrepare] blocks on a tokio runtime, so it runs on [Dispatchers.IO].
 * - [nativeRun] blocks for the lifetime of the session, so it gets a dedicated
 *   job on IO and is never awaited from the main thread.
 * - The engine calls back into [onNativeReady] and [protectSocket] from its own
 *   threads; both are thread-safe because they only touch volatile state and a
 *   forwarder to the service, which is thread-safe.
 */
class AuroraVpnEngine(
  private val app: Context,
  private val service: AuroraVpnService,
  private val statusSink: MutableStateFlow<VpnStatus>,
) : NativeEngineListener, NativeSocketProtector {

  constructor(service: AuroraVpnService, sink: MutableStateFlow<VpnStatus>) : this(
    app = service,
    service = service,
    statusSink = sink,
  )

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private var engineJob: Job? = null

  @Volatile private var prepared: PreparedEngine? = null

  val status: StateFlow<VpnStatus> = statusSink.asStateFlow()

  /**
   * Registers the socket protector before anything connects. The engine asks
   * for this on its first outbound socket — which can be during prepare, not
   * just during run — so installing it at connect time rather than at run time
   * is what keeps the handshake itself out of the tunnel.
   */
  init {
    NativeAetherBridge.setSocketProtector(this)
  }

  // ---- Public API -------------------------------------------------------

  /**
   * Starts a real connection: permission check, prepare, TUN, then run.
   *
   * The UI's connect is this; there is no other path into the engine.
   */
  fun connect(settings: VpnSettings, configPath: String, prepared: PreparedEngine? = null) {
    if (engineJob?.isActive == true) {
      Log.w(TAG, "CONNECT_REQUESTED ignored: engine already active")
      return
    }
    Log.i(TAG, "CONNECT_REQUESTED")
    engineJob = scope.launch { runEngine(settings, configPath, prepared) }
  }

  /**
   * Stops the engine. Always goes through [NativeAetherBridge.stop], which
   * signals the engine's stop channel; coroutine cancellation alone would
   * leave the tokio runtime and the TUN pump running.
   */
  fun stop() {
    Log.i(TAG, "NATIVE_STOP_REQUESTED")
    NativeAetherBridge.stop()
    // Give the engine a moment to observe the stop channel before we tear the
    // descriptor out from under it.
    scope.launch {
      engineJob?.join()
      statusSink.value = VpnStatus.Idle
      Log.i(TAG, "DISCONNECTED")
    }
  }

  /** True when [VpnService.prepare] would show the system permission dialog. */
  fun permissionRequired(): Boolean = VpnService.prepare(app) != null

  // ---- The real flow ----------------------------------------------------

  private suspend fun runEngine(settings: VpnSettings, configPath: String, preprepared: PreparedEngine?) {
    // 1. Permission. Without this, establish() returns null and there is no
    // point provisioning anything.
    if (permissionRequired()) {
      Log.i(TAG, "PERMISSION_REQUIRED")
      statusSink.value = VpnStatus.PermissionRequired
      return
    }
    Log.i(TAG, "VPN_PERMISSION_GRANTED")

    val config = settings.toBridgeJson(configPath, mode = "tun")

    // 2. Prepare: the engine resolves or provisions an identity and picks a
    // peer. This blocks on the engine's tokio runtime, so it is here on IO.
    statusSink.value = VpnStatus.Preparing
    Log.i(TAG, "PREPARE_STARTED")
    val resolved = preprepared ?: NativeAetherBridge.prepare(config).fold(
      onSuccess = { it },
      onFailure = { error ->
        Log.e(TAG, "PREPARE_ERROR: ${error.message}")
        statusSink.value = VpnStatus.Error(reason = error.message ?: "آماده‌سازی مسیر ناموفق بود")
        return
      },
    )
    prepared = resolved
    Log.i(TAG, "PREPARE_SUCCESS peer=${resolved.peer} ipv4=${resolved.ipv4} ipv6=${resolved.ipv6}")

    // 3. TUN. Only the platform can build this; the engine just pumps it.
    val tun = service.establishTun(resolved)
    if (tun == null) {
      Log.e(TAG, "TUN_ESTABLISH_FAILED")
      statusSink.value = VpnStatus.Error(reason = "ساخت رابط تونل ناموفق بود")
      return
    }
    Log.i(TAG, "TUN_ESTABLISHED fd=${tun.fd}")

    // 4. Run. This blocks for the whole session on this IO dispatcher.
    statusSink.value = VpnStatus.Connecting(attempt = 1)
    Log.i(TAG, "NATIVE_RUN_STARTED")
    NativeAetherBridge.run(
      configJson = config,
      preparedPeer = resolved.peer,
      tunFd = tun.fd,
      listener = this,
    ).fold(
      onSuccess = { Log.i(TAG, "NATIVE_RUN_RETURNED ok") },
      onFailure = { error ->
        Log.e(TAG, "NATIVE_RUN_RETURNED error: ${error.message}")
        statusSink.value = VpnStatus.Error(reason = error.message ?: "موتر با خطا متوقف شد")
      },
    )

    // 5. The run loop has ended. If it was not an error, it was a requested stop.
    if (statusSink.value !is VpnStatus.Error) {
      statusSink.value = VpnStatus.Idle
    }
  }

  // ---- Engine callbacks (invoked on engine threads) ---------------------

  /** The engine's run loop is up and the session is established. */
  override fun onNativeReady() {
    Log.i(TAG, "NATIVE_READY")
    val p = prepared
    statusSink.value = VpnStatus.Connected(
      endpoint = p?.peer,
      transport = null,
      usingFallback = false,
    )
  }

  /**
   * Routes one of the engine's sockets around the tunnel it created. Handing
   * this straight to [VpnService.protect] is the whole purpose: without it the
   * engine's own handshake traffic would loop back through the tunnel.
   */
  override fun protectSocket(fd: Int): Boolean {
    val protected = service.protect(fd)
    Log.d(TAG, "PROTECT_SOCKET fd=$fd -> $protected")
    return protected
  }

  fun dispose() {
    scope.cancel()
  }

  companion object {
    private const val TAG = "AuroraVpnEngine"
  }
}
