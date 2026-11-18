package com.example.smartdrill

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartdrill.ui.Disaster

object EmergencyNotifier {
    private const val CHANNEL_ID = "smart_drill_emergency_channel"

    // Campus center coordinates (DNSC reference point)
    private const val CAMPUS_LAT = 7.3135228
    private const val CAMPUS_LNG = 125.67033
    private const val MAX_CAMPUS_RADIUS_METERS = 1000.0 // 1 km radius geofence

    fun isInsideCampus(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            if (bestLocation != null) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    bestLocation.latitude, bestLocation.longitude,
                    CAMPUS_LAT, CAMPUS_LNG, results
                )
                return results[0] <= MAX_CAMPUS_RADIUS_METERS
            }
        } catch (_: SecurityException) {
        }
        return true
    }

    fun getSenderId(context: Context): String {
        val prefs = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("sender_id", null)
        if (id == null) {
            val randomNum = (1000..9999).random()
            id = "Sender-#$randomNum"
            prefs.edit().putString("sender_id", id).apply()
        }
        return id
    }

    fun getDeviceInfo(): String {
        val brand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val version = Build.VERSION.RELEASE
        return "$brand $model (Android $version)"
    }

    fun playSenderTone() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1200)
        } catch (_: Exception) {
        }
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Emergency Alerts"
            val descriptionText = "Critical emergency drill and disaster alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEmergencyAlert(context: Context, disaster: Disaster, senderId: String, locationInfo: String, deviceInfo: String) {
        createChannel(context)
        playSenderTone()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messageText = "🚨 $senderId | 📱 $deviceInfo"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("${disaster.alertTitle} ($senderId)")
            .setContentText(messageText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 ${disaster.alertTitle}\n👤 Sender ID: $senderId\n📱 Device: $deviceInfo\n📍 Location: $locationInfo\n\nSafety Steps:\n" + disaster.steps.joinToString("\n") { "• $it" })
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(1001, builder.build())
            }
        } catch (_: SecurityException) {
        }
    }

    fun cancelAlert(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.cancel(1001)
        } catch (_: SecurityException) {
        }
    }
}
