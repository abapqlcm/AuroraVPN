package com.auroravpn.app.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

/**
 * Owns the TUN interface for the lifetime of a connection.
 *
 * Phase 1 deliberately does not start the engine yet: the class exists so the
 * manifest entry, the foreground notification contract and the permission flow
 * are all in place and the APK has the shape of the finished app. Wiring the
 * Aether core to the descriptor is phase 2/3.
 *
 * The service is started by name from an Intent, which R8 cannot see, so it is
 * kept explicitly in proguard-rules.pro.
 */
class AuroraVpnService : VpnService() {

    @Volatile
    private var tunInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    private fun connect() {
        // Phase 2: ask the engine to prepare, then hand the fd here.
        // Establishing a placeholder interface now would capture traffic and
        // black-hole it, which is worse than not running at all.
        if (tunInterface != null) return
    }

    private fun disconnect() {
        tunInterface?.close()
        tunInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        // The system or the user revoked the VPN. The network is already being
        // restored by the platform; our job is to release the descriptor.
        disconnect()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    companion object {
        const val ACTION_CONNECT = "com.auroravpn.app.CONNECT"
        const val ACTION_DISCONNECT = "com.auroravpn.app.DISCONNECT"
    }
}
