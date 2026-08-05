package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class FlowForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun attachBaseContext(newBase: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            super.attachBaseContext(newBase.createAttributionContext("audio"))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                val intentName = intent?.getStringExtra(EXTRA_INTENT_NAME) ?: "Deep Focus"
                val remainingSeconds = intent?.getIntExtra(EXTRA_REMAINING_SECONDS, 1500) ?: 1500
                acquireWakeLock()
                val notification = buildNotification(intentName, remainingSeconds)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
            ACTION_UPDATE -> {
                val intentName = intent?.getStringExtra(EXTRA_INTENT_NAME) ?: "Deep Focus"
                val remainingSeconds = intent?.getIntExtra(EXTRA_REMAINING_SECONDS, 0) ?: 0
                val notification = buildNotification(intentName, remainingSeconds)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
            ACTION_STOP_AND_CANCEL_SESSION -> {
                FlowServiceController.requestCancelSession()
                stopForegroundService()
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FlowApp::ForegroundServiceWakeLock"
            ).apply {
                acquire(120 * 60 * 1000L) // 2 hours max timeout safety
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun stopForegroundService() {
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Flow Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification for active focus session, audio soundscape, and timer"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(intentName: String, remainingSeconds: Int): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FlowForegroundService::class.java).apply {
            action = ACTION_STOP_AND_CANCEL_SESSION
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (remainingSeconds > 0) {
            "$timeFormatted remaining • Soundscape & Timer Active"
        } else {
            "Session complete! Tap to reflect."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flow State: $intentName")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Session",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "flow_focus_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_UPDATE = "com.example.service.ACTION_UPDATE"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_STOP_AND_CANCEL_SESSION = "com.example.service.ACTION_STOP_AND_CANCEL_SESSION"

        const val EXTRA_INTENT_NAME = "extra_intent_name"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"

        fun startService(context: Context, intentName: String, remainingSeconds: Int) {
            try {
                val intent = Intent(context, FlowForegroundService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_INTENT_NAME, intentName)
                    putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("FlowForegroundService", "Failed to start foreground service", e)
            }
        }

        fun updateService(context: Context, intentName: String, remainingSeconds: Int) {
            try {
                val intent = Intent(context, FlowForegroundService::class.java).apply {
                    action = ACTION_UPDATE
                    putExtra(EXTRA_INTENT_NAME, intentName)
                    putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
                }
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("FlowForegroundService", "Failed to update foreground service", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, FlowForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("FlowForegroundService", "Failed to stop foreground service", e)
            }
        }
    }
}
