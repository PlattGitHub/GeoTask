package com.example.geotask

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * Started [Service] that works in foreground mode and implements Geofencing logic.
 *
 * @author Alexander Gorin
 */
class GeofencingService : Service() {

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }

    private val broadcastPendingIntent by lazy {
        Intent(this, GeofencingBroadcastReceiver::class.java).let { intent ->
            intent.action = BROADCAST_ACTION
            PendingIntent.getBroadcast(
                this,
                BROADCAST_INTENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIFICATION_ID,
            createNotification(this, getString(R.string.geofence_service_notification_text))
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val latitude = intent.getDoubleExtra(MapsActivity.LATITUDE_EXTRA, 0.0)
            val longitude = intent.getDoubleExtra(MapsActivity.LONGITUDE_EXTRA, 0.0)
            setupGeofences(latitude, longitude)
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun setupGeofences(latitude: Double, longitude: Double) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_REQUEST_ID)
            .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setNotificationResponsiveness(NOTIFICATION_RESPONSIVENESS_VALUE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()

        geofencingClient.addGeofences(geofencingRequest, broadcastPendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(
                    this@GeofencingService,
                    getString(R.string.geofence_toast_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
            addOnFailureListener {
                Toast.makeText(
                    this@GeofencingService,
                    getString(R.string.geofence_toast_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        geofencingClient.removeGeofences(broadcastPendingIntent)
    }

    private companion object {
        const val NOTIFICATION_ID = 111
        const val BROADCAST_INTENT_REQUEST_CODE = 124
        const val GEOFENCE_REQUEST_ID = "GEOFENCE_REQUEST_ID"
        const val GEOFENCE_RADIUS = 100F
        const val BROADCAST_ACTION = "com.example.geotask.GEOFENCE_ACTION"
        const val NOTIFICATION_RESPONSIVENESS_VALUE = 300000
    }
}