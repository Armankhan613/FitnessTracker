package com.example.fitnesstracker.data

class KalmanLatLong(private val qMetresPerSecond: Float) {
    private var minAccuracy = 1f
    private var variance = -1.0
    private var timestampMs: Long = 0
    private var lat = 0.0
    private var lng = 0.0

    fun process(lat_measurement: Double, lng_measurement: Double, accuracy: Float, time: Long) {
        println("Kalman filter processing started")
        if (accuracy < minAccuracy) minAccuracy = accuracy

        if (variance < 0) {
            this.lat = lat_measurement
            this.lng = lng_measurement
            variance = (accuracy * accuracy).toDouble()
        } else {
            val dt = (time - timestampMs) / 1000.0
            if (dt > 0) {
                variance += dt * qMetresPerSecond * qMetresPerSecond / 1000
            }

            val k = variance / (variance + (accuracy * accuracy))
            lat += k * (lat_measurement - lat)
            lng += k * (lng_measurement - lng)
            variance *= (1 - k)
        }
        timestampMs = time
        println("Kalman filter processing ended")
    }

    fun getLat(): Double {
        println("Latitude: $lat")
        return lat
    }
    fun getLng(): Double {
        println("Longitude: $lng")
        return lng
    }

}
