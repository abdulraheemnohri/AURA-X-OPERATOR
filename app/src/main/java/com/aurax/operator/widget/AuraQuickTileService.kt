package com.aurax.operator.widget

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.aurax.operator.app.MainActivity
import com.aurax.operator.core.security.SafetyController

/** Quick Settings entry point for emergency-stop and cockpit access. */
class AuraQuickTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.label = "AURA-X"
        qsTile?.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (SafetyController.isAbortRequested()) {
            SafetyController.clearAbort()
        } else {
            // Long-press behavior: open MainActivity
            startActivityAndCollapse(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        SafetyController.requestAbort("Quick Settings tile")
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.updateTile()
    }
}
