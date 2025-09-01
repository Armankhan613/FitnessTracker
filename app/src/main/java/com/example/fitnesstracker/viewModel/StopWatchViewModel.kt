package com.example.fitnesstracker.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopWatchViewModel(app: Application): AndroidViewModel(app) {
    private val _elapsedTime = MutableStateFlow(0L) // in ms
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private var stopwatchJob: Job? = null
    private var startTime = 0L

    fun startStopwatch() {
        if (stopwatchJob != null) return // already running

        startTime = System.currentTimeMillis() - _elapsedTime.value

        stopwatchJob = viewModelScope.launch {
            while (isActive) {
                _elapsedTime.value = System.currentTimeMillis() - startTime
                delay(100L)
            }
        }
    }

    fun pauseStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = null
    }

    fun resetStopwatch() {
        pauseStopwatch()
        _elapsedTime.value = 0L
    }
}