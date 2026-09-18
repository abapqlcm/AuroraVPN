package com.auroravpn.app.vpn

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

/**
 * Holds the tunnel state and drives its transitions.
 *
 * THIS IS THE SIMULATED ONE. Phase 4 replaces [connect] and [disconnect] with
 * calls into AuroraVpnService, which in turn calls nativeRun. The screens and
 * the state model stay exactly as they are.
 *
 * The simulation exists so the interaction and the motion can be judged before
 * the engine is live — a connect button that cannot be pressed because the
 * engine is not wired yet teaches us nothing about whether it feels right.
 */
class VpnController {

  private val _state = MutableStateFlow(VpnState.IDLE)
  val state: StateFlow<VpnState> = _state.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  fun toggle() {
    when (_state.value) {
      VpnState.IDLE, VpnState.ERROR -> connect()
      VpnState.CONNECTED -> disconnect()
      // Busy states are terminal from the user's point of view: the button is
      // disabled, so a stray tap cannot start a second handshake.
      else -> {}
    }
  }

  fun connect() {
    if (_state.value.isBusy) return
    _errorMessage.value = null
    _state.value = VpnState.REQUESTING
    scope.launch {
      // Android asks the user to approve the VPN session the first time.
      delay(420)
      if (!isActive) return@launch
      _state.value = VpnState.CONNECTING
      // Engine handshake. In the real build this is where nativeRun blocks.
      delay(1_900)
      if (!isActive) return@launch
      _state.value = VpnState.CONNECTED
    }
  }

  fun disconnect() {
    if (_state.value.isBusy) return
    _state.value = VpnState.DISCONNECTING
    scope.launch {
      delay(700)
      if (!isActive) return@launch
      _state.value = VpnState.IDLE
    }
  }

  /** Report a failure from the engine layer. Reserved for phase 4. */
  fun fail(reason: String) {
    _errorMessage.value = reason
    _state.value = VpnState.ERROR
  }

  /** Clear an error and return to idle. */
  fun acknowledgeError() {
    if (_state.value == VpnState.ERROR) {
      _errorMessage.value = null
      _state.value = VpnState.IDLE
    }
  }
}
