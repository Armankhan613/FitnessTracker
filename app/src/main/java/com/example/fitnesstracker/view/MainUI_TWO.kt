package com.example.fitnesstracker.view

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fitnesstracker.R
import com.example.fitnesstracker.viewModel.StepsCounterViewModel
import com.example.fitnesstracker.viewModel.StopWatchViewModel
import com.example.fitnesstracker.viewModel.locationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernScreen(modifier: Modifier = Modifier,stepViewModel: StepsCounterViewModel,locationViewModel: locationViewModel,stopWatchViewModel: StopWatchViewModel) {
    val context = LocalContext.current
    val elapsedTime by stopWatchViewModel.elapsedTime.collectAsState()
    var isTracking by remember { mutableStateOf(false) }
    // Load composition from raw resource
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.distance)
    )
    val compositionTwo by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.fire)
    )
    val compositionThree by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.clock_time_visible)
    )
    // Play the animation forever
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = isTracking,
        iterations = LottieConstants.IterateForever
    )
    var weight by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf(0.0) }
    var timeElapsed by remember { mutableStateOf(0L) }
    val distanceTravelled by locationViewModel.distanceTravelled.collectAsState()
    val userLocation by locationViewModel.location.collectAsState()
    var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    StartupScreen {
        weight = it
        println("Weight: $weight")
    }
    val weightKg = weight.toDoubleOrNull() ?: 0.0


    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val df = DecimalFormat("#.##")
    val dff=DecimalFormat("#.###")
    LaunchedEffect(userLocation) {
        userLocation?.let { currentLocation=LatLng(it.latitude, it.longitude) }
        println("currentLocation: $currentLocation")
    }

    LaunchedEffect(distanceTravelled) {

        caloriesBurned = weightKg * (distanceTravelled / 1000)
        println("caloriesBurned: $caloriesBurned")
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }
    LaunchedEffect(userLocation) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    DisposableEffect(Unit) {
        onDispose {
            locationViewModel.stopUpdate()
            stepViewModel.stop()
            stopWatchViewModel.resetStopwatch()
        }
    }


    if (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        &&ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    scrollGesturesEnabledDuringRotateOrZoom = false,
                    rotationGesturesEnabled = false,
                    compassEnabled = false
                )
            ) {
                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "You are here"
                )
            }

            // Bottom Card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    ,
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            ,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Row with C1, C2, C3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Column(modifier = Modifier.weight(1f).wrapContentWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                LottieAnimation(
                                    composition = composition,
                                    progress = { progress },
                                    modifier = Modifier
                                        .size(60.dp)   // 👈 control width & height
                                )
                                Text(
                                    text = dff.format(distanceTravelled/1000)+" Km",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,

                                )
                            }
                            Column(modifier = Modifier.weight(1f).wrapContentWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                LottieAnimation(
                                    composition = compositionTwo,
                                    progress = { progress },
                                    modifier = Modifier
                                        .size(60.dp)   // 👈 control width & height
                                )
                                Text(text = df.format(caloriesBurned)+" cal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f).wrapContentWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                LottieAnimation(
                                    composition = compositionThree,
                                    progress = { progress },
                                    modifier = Modifier
                                        .size(60.dp)   // 👈 control width & height
                                )
                                Text(text = formatTime(elapsedTime), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(70.dp))

                        // Start Button
                        Button(
                            onClick = {
                                    if (isTracking) {
                                        locationViewModel.stopUpdate()
                                        stopWatchViewModel.pauseStopwatch()
                                        isTracking = false
                                    } else {
                                        stepViewModel.startTracking()
                                        locationViewModel.startUpdate {  }
                                        stopWatchViewModel.startStopwatch()
                                        isTracking = true
                                    }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .wrapContentWidth()
                        ) {
                            Text(
                                text = if(isTracking) "Stop Running" else "Start Running",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60))
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
