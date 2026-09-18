package com.auroravpn.app.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Lets the user connect from the quick-settings shade without opening the app.
 *
 * Phase 1 registers the tile and keeps its state honest: it reports IDLE and
 * does not claim to be connected, because nothing can connect yet. Claiming a
 * state the app cannot deliver is a bug a user sees in the shade and cannot
 * dismiss.
 */
class AuroraTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        // Phase 2: VpnService.prepare() from a tile context cannot show the
        // system consent dialog, so this launches the main activity which can.
        val intent = Intent(this, Class.forName("com.auroravpn.app.MainActivity"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }

    private fun refreshTile() {
        qsTile?.apply {
            state = Tile.STATE_UNAVAILABLE
            label = "AuroraVPN"
            updateTile()
        }
    }
}
