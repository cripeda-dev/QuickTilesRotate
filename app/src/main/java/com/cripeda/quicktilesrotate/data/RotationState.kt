package com.cripeda.quicktilesrotate.data

import android.content.pm.ActivityInfo

enum class RotationState(val id: Int, val screenOrientation: Int) {
    OFF(0, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    LANDSCAPE(1, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
    PORTRAIT(2, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    companion object {
        fun fromId(id: Int): RotationState {
            return entries.firstOrNull { it.id == id } ?: OFF
        }

        fun nextState(current: RotationState): RotationState {
            return when (current) {
                OFF -> LANDSCAPE
                LANDSCAPE -> PORTRAIT
                PORTRAIT -> OFF
            }
        }
    }
}
