package com.cripeda.quicktilesrotate.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.cripeda.quicktilesrotate.R
import com.cripeda.quicktilesrotate.data.RotationPreferences
import com.cripeda.quicktilesrotate.data.RotationState
import com.cripeda.quicktilesrotate.ui.MainActivity

class OrientationOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var preferences: RotationPreferences
    private var overlayView: View? = null
    private var isViewAttached = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferences = RotationPreferences.getInstance(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val targetStateId = intent?.getIntExtra(EXTRA_ROTATION_STATE, -1) ?: -1
        val targetState = if (targetStateId != -1) {
            RotationState.fromId(targetStateId)
        } else {
            preferences.getRotationState()
        }

        applyRotationState(targetState)
        return START_NOT_STICKY
    }

    private fun applyRotationState(state: RotationState) {
        if (state == RotationState.OFF) {
            removeOverlay()
            preferences.setRotationState(RotationState.OFF)
            stopForegroundCompat()
            stopSelf()
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted. Cannot apply overlay.")
            removeOverlay()
            preferences.setRotationState(RotationState.OFF)
            stopForegroundCompat()
            stopSelf()
            return
        }

        // Start Foreground with notification
        val notification = buildNotification(state)
        startForegroundCompat(notification)

        // Apply WindowManager overlay with target orientation
        try {
            if (overlayView == null) {
                overlayView = View(this).apply {
                    alpha = 0f
                }
            }

            val layoutParams = WindowManager.LayoutParams(
                1,
                1,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                screenOrientation = state.screenOrientation
            }

            if (!isViewAttached) {
                windowManager.addView(overlayView, layoutParams)
                isViewAttached = true
            } else {
                windowManager.updateViewLayout(overlayView, layoutParams)
            }

            preferences.setRotationState(state)
            Log.d(TAG, "Applied orientation overlay: $state")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply WindowManager overlay", e)
            removeOverlay()
            preferences.setRotationState(RotationState.OFF)
            stopForegroundCompat()
            stopSelf()
        }
    }

    private fun removeOverlay() {
        if (isViewAttached && overlayView != null) {
            try {
                windowManager.removeViewImmediate(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view", e)
            }
            isViewAttached = false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(state: RotationState): Notification {
        val appIntent = Intent(this, MainActivity::class.java)
        val pendingAppIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val turnOffIntent = Intent(this, OrientationOverlayService::class.java).apply {
            putExtra(EXTRA_ROTATION_STATE, RotationState.OFF.id)
        }
        val pendingTurnOffIntent = PendingIntent.getService(
            this,
            1,
            turnOffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stateDescription = when (state) {
            RotationState.LANDSCAPE -> getString(R.string.state_landscape)
            RotationState.PORTRAIT -> getString(R.string.state_portrait)
            RotationState.OFF -> getString(R.string.state_off)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(stateDescription)
            .setSmallIcon(
                if (state == RotationState.LANDSCAPE) R.drawable.ic_rotate_landscape
                else R.drawable.ic_rotate_portrait
            )
            .setContentIntent(pendingAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_rotate_off,
                getString(R.string.action_turn_off),
                pendingTurnOffIntent
            )
            .build()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "OrientationOverlay"
        private const val CHANNEL_ID = "channel_screen_rotation_overlay"
        private const val NOTIFICATION_ID = 1001
        const val EXTRA_ROTATION_STATE = "extra_rotation_state"

        fun startWithState(context: Context, state: RotationState) {
            val intent = Intent(context, OrientationOverlayService::class.java).apply {
                putExtra(EXTRA_ROTATION_STATE, state.id)
            }
            if (state == RotationState.OFF) {
                context.startService(intent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
}
