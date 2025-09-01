package com.example.fitnesstracker.view

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.data.FitnessRepo
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.viewModel.StepsCounterViewModel
import com.example.fitnesstracker.viewModel.StopWatchViewModel
import com.example.fitnesstracker.viewModel.locationViewModel

class MainActivity : ComponentActivity() {
    private val stepViewModel: StepsCounterViewModel by viewModels()
    private val stopWatchViewModel:StopWatchViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo= FitnessRepo(stepViewModel)
        val locationViewModel: locationViewModel by viewModels(){
            LocationViewModelFactory(repo,application)
        }

        enableEdgeToEdge(
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(
                scrim = Color.Black.toArgb() // Nav bar background
            ),
            statusBarStyle = androidx.activity.SystemBarStyle.dark(
                Color.Black.toArgb(),
            )
        )
        setContent {
            FitnessTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    println("APP STARTED THROUGH MAIN ACTIVITY")
                    ModernScreen(modifier = Modifier.padding(innerPadding), stepViewModel =stepViewModel, locationViewModel = locationViewModel, stopWatchViewModel = stopWatchViewModel )
                }
            }
        }
    }
}
class LocationViewModelFactory(
    private val repo: FitnessRepo,
    private val app:Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(locationViewModel::class.java)) {
            return locationViewModel(app=app,repo=repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//class StepViewModelFactory(
//    private val app:Application,
//    private val repo: FitnessRepo,
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(StepsCounterViewModel::class.java)) {
//            return StepsCounterViewModel(app=app,repo=FitnessRepo()) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}



