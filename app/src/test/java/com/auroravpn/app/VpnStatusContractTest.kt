package com.auroravpn.app

import com.auroravpn.app.vpn.VpnStatus
import com.auroravpn.app.vpn.isBusy
import com.auroravpn.app.vpn.isProtecting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The state model is the contract the UI and the engine layer both code
 * against, so it is worth pinning down. The engine writes these values; the UI
 * is a pure function of them. If the two disagree about what a state means the
 * app shows the wrong thing for a real connection.
 */
class VpnStatusContractTest {

  @Test fun idle_is_neither_busy_nor_protecting() {
    assertFalse(VpnStatus.Idle.isBusy)
    assertFalse(VpnStatus.Idle.isProtecting)
  }

  @Test fun preparing_is_busy_but_not_protecting() {
    assertTrue(VpnStatus.Preparing.isBusy)
    assertFalse(VpnStatus.Preparing.isProtecting)
  }

  @Test fun connecting_is_busy_but_not_protecting() {
    // The engine is handshaking. Showing "secure" here would be a claim the
    // tunnel has not made yet.
    val connecting = VpnStatus.Connecting(attempt = 1)
    assertTrue(connecting.isBusy)
    assertFalse(connecting.isProtecting)
  }

  @Test fun connected_is_protecting_but_not_busy() {
    // Only onNativeReady sets this, and it means the tunnel is actually up.
    val connected = VpnStatus.Connected(
      endpoint = "162.159.36.1:443",
      transport = "masque",
      usingFallback = false,
    )
    assertTrue(connected.isProtecting)
    assertFalse(connected.isBusy)
  }

  @Test fun disconnecting_is_busy_but_not_protecting() {
    // The tunnel is going down; the UI must not still imply protection.
    assertTrue(VpnStatus.Disconnecting.isBusy)
    assertFalse(VpnStatus.Disconnecting.isProtecting)
  }

  @Test fun error_is_neither_and_carries_a_reason() {
    val error = VpnStatus.Error(reason = "no clean endpoint found")
    assertFalse(error.isBusy)
    assertFalse(error.isProtecting)
    assertEquals("no clean endpoint found", error.reason)
  }

  @Test fun permission_required_is_neither() {
    // Not an error: the user has not refused anything, the system just has not
    // been asked yet. Treating it as an error would show a failure message to
    // someone who has only ever opened the app.
    assertFalse(VpnStatus.PermissionRequired.isBusy)
    assertFalse(VpnStatus.PermissionRequired.isProtecting)
  }
}
