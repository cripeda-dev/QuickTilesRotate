package com.cripeda.quicktilesrotate.data

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.service.quicksettings.TileService
import com.cripeda.quicktilesrotate.service.RotationTileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RotationPreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _stateFlow = MutableStateFlow(getRotationState())
    val stateFlow: StateFlow<RotationState> = _stateFlow.asStateFlow()

    fun getRotationState(): RotationState {
        val id = prefs.getInt(KEY_ROTATION_STATE, RotationState.OFF.id)
        return RotationState.fromId(id)
    }

    fun setRotationState(state: RotationState) {
        prefs.edit().putInt(KEY_ROTATION_STATE, state.id).apply()
        _stateFlow.value = state

        // Update Quick Settings Tile
        try {
            TileService.requestListeningState(
                context,
                ComponentName(context, RotationTileService::class.java)
            )
        } catch (e: Exception) {
            // Can be ignored if tile is not placed
        }
    }

    companion object {
        private const val PREFS_NAME = "quick_tiles_rotate_prefs"
        private const val KEY_ROTATION_STATE = "key_rotation_state"

        @Volatile
        private var instance: RotationPreferences? = null

        fun getInstance(context: Context): RotationPreferences {
            return instance ?: synchronized(this) {
                instance ?: RotationPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
