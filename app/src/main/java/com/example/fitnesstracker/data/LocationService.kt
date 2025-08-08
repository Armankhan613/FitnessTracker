package com.example.fitnesstracker.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmoothedLocationService(
    private val context: Context,
    private val movementThreshold: Float = 3f, // meters
    private val accuracyThreshold: Float = 20f // meters
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val kalmanFilter = KalmanLatLong(3f) // expected max speed = 3 m/s

    private var lastLocation: Location? = null

    data class LocationUpdate(val location: Location, val totalDistance: Float)

    private var totalDistance = 0f

    fun startLocationUpdates(onLocation: (LocationUpdate) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
            return
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000 // every 2 sec
        ).setMinUpdateDistanceMeters(1f) // raw GPS min distance
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val rawLocation = result.lastLocation ?: return

                // Accuracy filter
                if (rawLocation.accuracy > accuracyThreshold) return

                // Pass through Kalman filter
                kalmanFilter.process(
                    rawLocation.latitude,
                    rawLocation.longitude,
                    rawLocation.accuracy,
                    System.currentTimeMillis()
                )

                val filteredLat = kalmanFilter.getLat()
                val filteredLng = kalmanFilter.getLng()

                val filteredLocation = Location("").apply {
                    latitude = filteredLat
                    longitude = filteredLng
                }

                // Distance filter
                lastLocation?.let { prev ->
                    val dist = prev.distanceTo(filteredLocation)
                    if (dist >= movementThreshold) {
                        lastLocation = filteredLocation
                        CoroutineScope(Dispatchers.Main).launch {
                            totalDistance += dist
                            onLocation(LocationUpdate(filteredLocation, totalDistance))
                        }
                    }
                } ?: run {
                    lastLocation = filteredLocation
                    CoroutineScope(Dispatchers.Main).launch {
                        onLocation(LocationUpdate(filteredLocation, totalDistance))
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }
}
