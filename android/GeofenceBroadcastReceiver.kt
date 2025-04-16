package com.example.gpsgeofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) return

        for (geofence in geofencingEvent.triggeringGeofences) {
            Log.d("GEOFENCE", "Geofence ativada: ${geofence.requestId}")
        }
    }
}
