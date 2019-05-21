package com.example.geotask

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * [BroadcastReceiver] subclass that triggers when user enters geofence area.
 * Registered in manifest.
 *
 * @author Alexander Gorin
 */
class GeofencingBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                NOTIFICATION_ID,
                createNotification(
                    context,
                    String.format(
                        context.getString(R.string.broadcast_notification_text),
                        geofencingEvent.triggeringLocation.latitude.toString(),
                        geofencingEvent.triggeringLocation.longitude.toString()
                    )
                )
            )
        }

    }

    private companion object {
        const val NOTIFICATION_ID = 444
    }
}