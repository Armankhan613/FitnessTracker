package com.example.fitnesstracker.Test
//import android.Manifest
//import android.app.Activity
//import android.content.Context
//import android.content.pm.PackageManager
//import android.hardware.SensorManager
//import android.location.Location
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import com.example.fitnesstracker.data.SmoothedLocationService
//import com.example.fitnesstracker.Test.StepCounterHelper
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.maps.android.compose.*
//
//@Composable
//fun RealTimeLocationScreen(modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    val sensorManager=remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
//    val locationService = remember { SmoothedLocationService(context = context) }
//    val stepsCounter= remember { StepCounterHelper(context as Activity,sensorManager,0.8f) }
//    var prevLocation by remember { mutableStateOf<Location?>(null) }
//    var currentLocation by remember { mutableStateOf<Location?>(null) }
//    var distance by remember { mutableStateOf(0f) }
//    var isTracking by remember { mutableStateOf(false) }
//    var userLocation by remember { mutableStateOf(LatLng(28.6139, 77.2090)) }
//    var accuracy by remember { mutableStateOf(0f) }
//
//    val permission = Manifest.permission.ACCESS_FINE_LOCATION
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { granted ->
//        if (granted) {
//            isTracking = true
//            locationService.startLocationUpdates {
//                prevLocation = currentLocation
//                currentLocation = it.location
//                accuracy=it.accuracy
//                if(accuracy>6f){
//                    distance+=stepsCounter.totalDistance
//                    println("updated by accelerometer")
//                }
//                else{
//                    stepsCounter.steps=0
//                    stepsCounter.totalDistance=0f
//                    distance += it.totalDistance
//                    println("updated by gps")
//                }
//                userLocation = LatLng(it.location.latitude, it.location.longitude)
//            }
//        } else {
//            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
//        if (granted) {
//            isTracking = true
//            locationService.startLocationUpdates {
//                prevLocation = currentLocation
//                currentLocation = it.location
//                accuracy=it.accuracy
//                if(accuracy>6f){
//                    distance+=stepsCounter.totalDistance
//                    Log.d("Stepscounter","Steps: ${stepsCounter.steps}")
//                }
//                else{
//                    stepsCounter.steps=0
//                    distance += it.totalDistance
//                    Log.d("gps update","")
//                }
//                userLocation = LatLng(it.location.latitude, it.location.longitude)
//            }
//        } else {
//            permissionLauncher.launch(permission)
//        }
//    }
//
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
//    }
//    LaunchedEffect(userLocation) {
//        cameraPositionState.animate(
//            update = CameraUpdateFactory.newCameraPosition(
//                CameraPosition.fromLatLngZoom(userLocation, 15f)
//            ),
//            durationMs = 1000
//        )
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            locationService.stopLocationUpdates()
//            stepsCounter.stop()
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()
//        .navigationBarsPadding().padding(0.dp,45.dp,0.dp,0.dp)) {
//        GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            cameraPositionState = cameraPositionState,
//            properties = MapProperties(isMyLocationEnabled = true),
//            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false,
//                zoomGesturesEnabled = false, scrollGesturesEnabled = false, tiltGesturesEnabled = false,
//                scrollGesturesEnabledDuringRotateOrZoom = false,rotationGesturesEnabled = false,
//                compassEnabled=false)
//        ) {
//            Marker(
//                state = MarkerState(position = userLocation),
//                title = "You are here"
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .background(Color.White.copy(alpha = 0.8f)) // semi-transparent background
//                .padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Previous Location:", color = Color.Black, fontSize = 16.sp)
//            Text("Latitude: ${prevLocation?.latitude ?: "Waiting..."}", color = Color.Black)
//            Text("Longitude: ${prevLocation?.longitude ?: "Waiting..."}", color = Color.Black)
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Text("Current Location:", color = Color.Black, fontSize = 16.sp)
//            Text("Latitude: ${currentLocation?.latitude ?: "Waiting..."}", color = Color.Black)
//            Text("Longitude: ${currentLocation?.longitude ?: "Waiting..."}", color = Color.Black)
//
//            Spacer(modifier = Modifier.height(12.dp))
//            Text("Distance: $distance meters", color = Color.Black)
//
//            Spacer(modifier = Modifier.height(12.dp))
//            Button(onClick = {
//                if (isTracking) {
//                    locationService.stopLocationUpdates()
//                    isTracking = false
//                } else {
//                    permissionLauncher.launch(permission)
//                }
//            }) {
//                Text(if (isTracking) "Stop Tracking" else "Start Tracking")
//            }
//        }
//    }
//}
