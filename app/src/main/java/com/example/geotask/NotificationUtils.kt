package com.example.geotask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

fun createNotification(context: Context, contextText: String): Notification {
    val channelID = createNotificationChannel(context)
    return NotificationCompat.Builder(context, channelID)
        .setSmallIcon(R.drawable.ic_map)
        .setContentTitle(context.getString(R.string.geofencing_notification_title))
        .setContentText(contextText)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(false)
        .setContentIntent(Intent(context, MapsActivity::class.java).let { intent ->
            PendingIntent.getActivity(context, MAP_INTENT_REQUEST_CODE, intent, 0)
        }).build()
}

private fun createNotificationChannel(context: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        return CHANNEL_ID
    }
    return ""
}

private const val CHANNEL_ID = "CHANNEL_ID"
private const val MAP_INTENT_REQUEST_CODE = 123