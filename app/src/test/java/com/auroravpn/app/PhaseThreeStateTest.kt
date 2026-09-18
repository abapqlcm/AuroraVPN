package com.auroravpn.app

import com.auroravpn.app.vpn.VpnState
import com.auroravpn.app.vpn.isBusy
import com.auroravpn.app.vpn.isProtecting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The state model is the contract the UI and the engine layer both code
 * against, so it is worth pinning down. Phase 4 replaces the controller's
 * internals; these expectations must not change.
 */
class PhaseThreeStateTest {

  @Test fun idle_is_neither_busy_nor_protecting() {
    assertFalse(VpnState.IDLE.isBusy)
    assertFalse(VpnState.IDLE.isProtecting)
  }

  @Test fun busy_states_are_busy_but_not_protecting() {
    listOf(VpnState.REQUESTING, VpnState.CONNECTING, VpnState.DISCONNECTING).forEach { s ->
      assertTrue("$s should be busy", s.isBusy)
      assertFalse("$s should not be protecting", s.isProtecting)
    }
  }

  @Test fun connected_is_protecting_but_not_busy() {
    assertTrue(VpnState.CONNECTED.isProtecting)
    assertFalse(VpnState.CONNECTED.isBusy)
  }

  @Test fun error_is_neither() {
    assertFalse(VpnState.ERROR.isBusy)
    assertFalse(VpnState.ERROR.isProtecting)
  }
}
