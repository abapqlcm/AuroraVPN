package com.auroravpn.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.auroravpn.app.ui.AuroraApp
import com.auroravpn.app.ui.theme.AuroraTheme

class MainActivity : ComponentActivity() {

    private val vpnPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val granted = result.resultCode == Activity.RESULT_OK
        Log.i(TAG, "VPN_PERMISSION_GRANTED=$granted")
        appController?.onPermissionResult(granted)
    }

    private var appController: com.auroravpn.app.vpn.VpnController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = (application as AuroraApplication).vpnController
        appController = controller

        // The tunnel status screen is the whole app, so it draws behind the
        // system bars rather than sitting in a letterbox.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        setContent {
            AuroraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AuroraApp(
                        controller = controller,
                        onRequestVpnPermission = { requestVpnPermission() },
                    )
                }
            }
        }
    }

    /**
     * Hands the user to Android's own VPN permission dialog. There is no custom
     * permission screen: duplicating the system dialog would leave the two able
     * to disagree about what the user actually allowed.
     */
    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            Log.i(TAG, "PERMISSION_DIALOG shown")
            vpnPermission.launch(intent)
        } else {
            // prepare() returning null means the system already consents.
            Log.i(TAG, "VPN_PERMISSION_ALREADY_GRANTED")
            appController?.onPermissionResult(true)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
