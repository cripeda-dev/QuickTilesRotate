package com.cripeda.quicktilesrotate.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.cripeda.quicktilesrotate.R
import com.cripeda.quicktilesrotate.data.RotationPreferences
import com.cripeda.quicktilesrotate.data.RotationState
import com.cripeda.quicktilesrotate.ui.MainActivity

class RotationTileService : TileService() {

    private lateinit var preferences: RotationPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = RotationPreferences.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileUI(preferences.getRotationState())
    }

    override fun onClick() {
        super.onClick()

        // Verify overlay permission first
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val currentState = preferences.getRotationState()
        val nextState = RotationState.nextState(currentState)

        // Apply new state via Service
        OrientationOverlayService.startWithState(this, nextState)

        // Optimistically update tile immediately for instant feedback
        updateTileUI(nextState)
    }

    private fun updateTileUI(state: RotationState) {
        val tile = qsTile ?: return

        when (state) {
            RotationState.OFF -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.tile_label_off)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_subtitle_off)
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_rotate_off)
            }
            RotationState.LANDSCAPE -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.tile_label_landscape)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_subtitle_landscape)
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_rotate_landscape)
            }
            RotationState.PORTRAIT -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.tile_label_portrait)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_subtitle_portrait)
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_rotate_portrait)
            }
        }

        tile.updateTile()
    }
}
