package com.example.fitnesstracker.Test
//package com.example.fitnesstracker.Test
//
//import android.Manifest
//import android.app.Activity
//import android.content.pm.PackageManager
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//class StepCounterHelper(
//    private val activity: Activity,
//    private val sensorManager: SensorManager,
//    private val strideLength: Float
//) : SensorEventListener {
//
//    companion object {
//        const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001
//    }
//
//    var steps = 0
//    var totalDistance = 0f
//
//    fun start() {
//        // Check for ACTIVITY_RECOGNITION permission
//        if (ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.ACTIVITY_RECOGNITION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request the permission
//            ActivityCompat.requestPermissions(
//                activity,
//                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
//                PERMISSION_REQUEST_ACTIVITY_RECOGNITION
//            )
//            return
//        }
//
//        // Permission already granted, start step detection
//        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
//        stepSensor?.also {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//    }
//
//    fun stop() {
//        sensorManager.unregisterListener(this)
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
//            steps++
//            totalDistance = steps * strideLength
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        // Not used in this case
//    }
//}
