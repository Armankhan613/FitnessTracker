package com.example.fitnesstracker.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmoothedLocationService(
    private val context: Context,
    private val movementThreshold: Float = 1f, // meters
    private val accuracyThreshold: Float = 12f, // meters
    private val maxSpeedThreshold: Float = 15f // meters/sec, ~54 km/h
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val kalmanFilter = KalmanLatLong(3f) // expected max speed = 3 m/s

    private var lastLocation: Location? = null
    private var lastTimestamp: Long = 0L
    private var totalDistance = 0.0

    data class LocationUpdate(val location: Location, val totalDistance: Double,val accuracy:Float)

    fun startLocationUpdates(onLocation: (LocationUpdate) -> Unit) {
        println("Location Update started from LOCATION SERVICE")
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
            return
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).setMinUpdateDistanceMeters(0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val rawLocation = result.lastLocation ?: return

                // Accuracy filter
                if (rawLocation.accuracy >=accuracyThreshold) {
//                    totalDistance=0.0
                    println("Accuracy too high: ${rawLocation.accuracy}")
                }

                val currentTime = System.currentTimeMillis()

                // Kalman filter update
                kalmanFilter.process(
                    rawLocation.latitude,
                    rawLocation.longitude,
                    rawLocation.accuracy,
                    currentTime
                )

                val filteredLat = kalmanFilter.getLat()
                val filteredLng = kalmanFilter.getLng()

                val filteredLocation = Location("").apply {
                    latitude = filteredLat
                    longitude = filteredLng
                    time = currentTime
                }

                // If no lastLocation, initialize and emit without distance
                if (lastLocation == null) {
                    lastLocation = filteredLocation
                    lastTimestamp = currentTime
                    println("Location at START: $filteredLocation")
                    CoroutineScope(Dispatchers.Main).launch {
                        onLocation(LocationUpdate(filteredLocation, totalDistance,rawLocation.accuracy))
                    }
                    return
                }

                // Calculate speed in m/s between last and current
                val timeDelta = (filteredLocation.time - lastTimestamp) / 1000f
                val distanceDelta = lastLocation!!.distanceTo(filteredLocation)
                val speed = if (timeDelta > 0) distanceDelta / timeDelta else 0f

                // Filter out unrealistic speed spikes
                if (speed > maxSpeedThreshold) {
                    println("Speed too high: $speed")
                    return
                }

                // Only update if moved more than threshold
                    lastLocation = filteredLocation
                    lastTimestamp = currentTime
                    CoroutineScope(Dispatchers.Main).launch {
                        if(rawLocation.accuracy<=8f){
                        totalDistance += distanceDelta
                        println("Total Distance from LOCATION SERVICE: $totalDistance")
                        }
                        else
                            totalDistance=0.0
                        onLocation(LocationUpdate(filteredLocation, totalDistance,rawLocation.accuracy))
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
        println("Location Update stopped")
    }
}

