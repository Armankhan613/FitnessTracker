package com.example.fitnesstracker.viewModel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.FitnessRepo
import com.example.fitnesstracker.data.SmoothedLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class locationViewModel(app: Application,repo: FitnessRepo) : AndroidViewModel(app) {

    private val locationService = SmoothedLocationService(app)
    private val myRepo: FitnessRepo=repo

    private var lastDistanceByGPS = 0.0
    private var lastDistanceBySteps = 0.0
    private var isContinuous = false

    private val _distanceTravelled = MutableStateFlow(0.0)
    val distanceTravelled = _distanceTravelled.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val _accuracy = MutableStateFlow(0f)
    val accuracy = _accuracy.asStateFlow()

    data class LocationUpdate(val location: Location, val totalDistance: Double, val accuracy: Float)

    init {
        println("LocationViewModel Created")
    }

    fun startUpdate(getLoc: (LocationUpdate) -> Unit) {
        locationService.startLocationUpdates { update ->
            println("Location at Present: ${update.location}")
            val currentAccuracy = update.accuracy
            _location.value = update.location
            _accuracy.value = currentAccuracy

            val deltaDistance = if (currentAccuracy <= 8f) {
                // Good GPS, use GPS delta
                println("GOOD GPS")
                val gpsDelta = update.totalDistance
//                lastDistanceByGPS = update.totalDistance
                isContinuous = false
                gpsDelta
            } else {
                // Poor GPS, use step counter delta
                println("POOR GPS")
                if (!isContinuous) {
                    myRepo.reset()
                    println("distance reset")
//                    lastDistanceBySteps = 0.0
                    isContinuous = true
                }
                val stepDelta = myRepo.distance.value
                println("stepDelta:$stepDelta")
//                lastDistanceBySteps = stepViewModel.distance.value
                stepDelta
            }

            _distanceTravelled.value += deltaDistance
            myRepo.reset()
            println("Distance travelled: ${_distanceTravelled.value}")
            getLoc(LocationUpdate(update.location, _distanceTravelled.value, currentAccuracy))
        }
    }

    fun stopUpdate() {
        locationService.stopLocationUpdates()
        myRepo.stop()
        println("LocationService and Sensor stopped")
    }
}
