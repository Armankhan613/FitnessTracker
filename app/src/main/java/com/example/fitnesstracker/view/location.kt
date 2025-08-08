package com.example.fitnesstracker.view

//import androidx.compose.runtime.Composable
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.data.LocationService
import com.example.fitnesstracker.data.SmoothedLocationService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.round

//import com.google.accompanist.permissions.rememberPermissionState
//import com.google.accompanist.permissions.ExperimentalPermissionsApi


@Composable
fun RealTimeLocationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locationService = remember { SmoothedLocationService(context) }
    var prevLocation by remember { mutableStateOf<Location?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var distance by remember { mutableStateOf(0f) }
    var isTracking by remember { mutableStateOf(false) }

    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isTracking = true
            locationService.startLocationUpdates {
                prevLocation = currentLocation
                currentLocation = it.location
                distance=it.totalDistance
            }
        } else {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            isTracking = true
            locationService.startLocationUpdates {
                prevLocation = currentLocation
                currentLocation = it.location
                distance=it.totalDistance
            }
        } else {
            permissionLauncher.launch(permission)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationService.stopLocationUpdates()
        }
    }

    Column(Modifier.fillMaxSize().padding(26.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Text("Previous Location:")
        Text("Latitude: ${prevLocation?.latitude ?: "Waiting..."}")
        Text("Longitude: ${prevLocation?.longitude ?: "Waiting..."}")

        Spacer(modifier = Modifier.height(16.dp))
        Text("Current Location:")
        Text("Latitude: ${currentLocation?.latitude ?: "Waiting..."}")
        Text("Longitude: ${currentLocation?.longitude ?: "Waiting..."}")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Distance: $distance meters")
        Spacer(modifier = Modifier.height(16.dp))


        Button(onClick = {
            if (isTracking) {
                locationService.stopLocationUpdates()
                isTracking = false
            } else {
                permissionLauncher.launch(permission)
            }
        }) {
            Text(if (isTracking) "Stop Tracking" else "Start Tracking")
        }
    }
}


