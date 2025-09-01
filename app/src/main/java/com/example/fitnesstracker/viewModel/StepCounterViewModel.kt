package com.example.fitnesstracker.viewModel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.FitnessRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StepsCounterViewModel(app: Application): AndroidViewModel(app), SensorEventListener {

    private val sensorManager= app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val appContext=app

    private var stepSensor:Sensor?=null
    private var isRegistered = false

    private var initialStepsCount = -1

    private val _steps= MutableStateFlow(0)
    val steps=_steps.asStateFlow()

    private val _distance= MutableStateFlow(0.0) //in meters
    val distance=_distance.asStateFlow()

    private val _calories= MutableStateFlow(0.0) //in Kcal
    val calories=_calories.asStateFlow()

    init{
        println("Step ViewModel created")
    }

    fun startTracking() {
        if (isRegistered) return // avoid double registration

        if(ActivityCompat.checkSelfPermission(
                appContext,android.Manifest.permission.ACTIVITY_RECOGNITION)== PackageManager.PERMISSION_GRANTED
        ){
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

            stepSensor?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
                isRegistered = true
                println("Sensor registered")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type==Sensor.TYPE_STEP_COUNTER){
            val totalSteps=event.values[0].toInt()
            if(initialStepsCount==-1){
                initialStepsCount=totalSteps
            }
            val currentSteps=totalSteps-initialStepsCount
            val stepLengthMeters = 0.78 // average stride length in meters
            val distanceMeters = currentSteps * stepLengthMeters
            val caloriesBurned = currentSteps * 0.04 // very rough estimate

            viewModelScope.launch {
                _steps.emit(currentSteps)
                _distance.emit(distanceMeters)
                _calories.emit(caloriesBurned)
                println("step counter distance:${_distance.value}")
            }
            println("Steps: ${currentSteps}, Distance: ${distanceMeters/1000} km, Calories: $caloriesBurned")

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stop() {
        sensorManager.unregisterListener(this)
        isRegistered=false
        println("Sensor unregistered")
    }

    fun resetDistance() {
        initialStepsCount=-1
        println("Distance reset and initial steps count set to $initialStepsCount")
    }

//    fun registerSensorAgain(){
//        if(ActivityCompat.checkSelfPermission(
//                appContext,android.Manifest.permission.ACTIVITY_RECOGNITION)== PackageManager.PERMISSION_GRANTED
//        ){
//            stepSensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//            stepSensor?.let{
//                sensorManager.registerListener(this,it, SensorManager.SENSOR_DELAY_UI)
//            }
//        }
//    }
}