package com.auroravpn.app

import android.app.Application
import com.auroravpn.app.vpn.VpnController

/**
 * Owns the one [VpnController] for the process lifetime.
 *
 * The controller and the status sink have to survive an Activity recreation —
 * rotating the phone must not drop the tunnel's state — and they have to be
 * reachable from the service as well as the UI. Holding both here is what makes
 * the service and the Activity see the same tunnel.
 */
class AuroraApplication : Application() {

    val vpnController: VpnController by lazy {
        VpnController(this)
    }
}
