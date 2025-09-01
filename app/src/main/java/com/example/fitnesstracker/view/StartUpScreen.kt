package com.example.fitnesstracker.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun StartupScreen(getWeight: (String) -> Unit) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(true) }

    // step: 0 = ask activity permission, 1 = ask location, 2 = show alert
    var step by rememberSaveable { mutableStateOf(0) }

    // Launcher for Location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            step = 2
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for Activity Recognition permission
    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Now check / request Location
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                step = 2
            }
        } else {
            Toast.makeText(context, "Physical activity permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // 🚀 Trigger Activity permission once (only when Composable first shows)
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            // If already granted → move to Location
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                step = 2
            }
        }
    }

    // ✅ Alert box after both permissions are granted
    if (step == 2) {
        if(showDialog){
            AlertDialog(
                onDismissRequest = {},
                title = { Text("User Input Required") },
                text = {
                    Column {
                        Text("Please enter your weight in kilograms:")
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Enter here...") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { getWeight(inputText)
                        if(inputText.isNotEmpty()&&inputText.isDigitsOnly())
                        {showDialog=false}
                        else{
                            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                        }
                        }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
