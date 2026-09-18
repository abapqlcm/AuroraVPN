package com.auroravpn.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.auroravpn.app.AuroraApplication
import com.auroravpn.app.MainActivity
import com.auroravpn.app.R
import com.auroravpn.app.core.PreparedEngine
import com.auroravpn.app.vpn.AuroraVpnEngine
import com.auroravpn.app.vpn.VpnSettings
import java.io.File

/**
 * Owns the TUN interface for the lifetime of a connection.
 *
 * Android insists that the tunnel interface come from [VpnService.Builder], not
 * from the engine: a process cannot open /dev/tun without the platform's
 * permission, and only an established VpnService gets the routes. So this class
 * builds the descriptor and hands its fd to the engine, which pumps packets
 * through it.
 *
 * The service is started by name from an Intent, which R8 cannot see, so it is
 * kept explicitly in proguard-rules.pro.
 */
class AuroraVpnService : VpnService() {

    @Volatile
    private var tunInterface: ParcelFileDescriptor? = null

    @Volatile
    private var engine: AuroraVpnEngine? = null

    /**
     * The one place the engine's identity and state live. The directory must be
     * writable by this app, so it is under filesDir — never an asset path,
     * which is read-only on a packaged APK.
     */
    val configPath: String by lazy {
        File(filesDir, "aether.toml").absolutePath
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect(intent)
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    private fun connect(intent: Intent) {
        if (tunInterface != null) return
        startForegroundCompat()

        val controller = (application as AuroraApplication).vpnController
        controller.attachService(this)

        engine = AuroraVpnEngine(this, controller.statusSink).also { engine ->
            engine.connect(
                settings = VpnSettings.fromPrefs(this),
                configPath = configPath,
            )
        }
    }

    /**
     * Builds the TUN descriptor from what the engine actually resolved.
     *
     * The addresses come from [PreparedEngine], which is what nativePrepare
     * reported — not from constants. The routes cover both families when the
     * engine returned an IPv6 address, because a dual-stack tunnel that only
     * routes IPv4 leaves the rest of the device exposed.
     */
    fun establishTun(prepared: PreparedEngine): ParcelFileDescriptor? {
        if (tunInterface != null) return tunInterface

        val builder = Builder()
            .setSession(SESSION_NAME)
            .setMtu(MTU)
            .setVpnAddresses(prepared)

        // Route everything: this is whole-device capture, and the engine's own
        // outbound sockets are kept out of it by [protect].
        builder.addRoute("0.0.0.0", 0)
        if (prepared.ipv6.isNotBlank()) {
            builder.addRoute("::", 0)
        }

        // DNS through the tunnel's own resolver when the engine has one.
        builder.addDnsServer("1.1.1.1")
        if (prepared.ipv6.isNotBlank()) {
            builder.addDnsServer("2606:4700:4700::1111")
        }

        val tun = builder.establish() ?: return null
        tunInterface = tun
        Log.i(TAG, "TUN established; fd=${tun.fd}")
        return tun
    }

    override fun onRevoke() {
        // The system or the user revoked the VPN. The network is already being
        // restored by the platform; our job is to stop the engine and release
        // the descriptor.
        disconnect()
    }

    /**
     * Tears the whole session down. Idempotent: the engine's stop and the fd
     // close are both safe to call when nothing is running.
     */
    fun disconnect() {
        Log.i(TAG, "DISCONNECT requested")
        engine?.stop()
        engine = null
        tearDown()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun tearDown() {
        tunInterface?.let {
            runCatching { it.close() }
            Log.i(TAG, "TUN closed")
        }
        tunInterface = null
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    private fun VpnService.Builder.setVpnAddresses(prepared: PreparedEngine): VpnService.Builder {
        if (prepared.ipv4.isNotBlank()) addAddress(prepared.ipv4, 32)
        if (prepared.ipv6.isNotBlank()) addAddress(prepared.ipv6, 128)
        return this
    }


    /**
     * Posts the foreground notification. From API 34 the platform requires the
     * service type to be named in the call as well as the manifest, and it must
     * match the declared type or the post is refused.
     */
    private fun startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, buildNotification(), FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.vpn_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                )
            )
        }
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.vpn_notification_connecting))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "AuroraVpnService"
        private const val SESSION_NAME = "AuroraVPN"
        private const val MTU = 1420
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "auroravpn.tunnel"

        const val ACTION_CONNECT = "com.auroravpn.app.CONNECT"
        const val ACTION_DISCONNECT = "com.auroravpn.app.DISCONNECT"
    }
}
