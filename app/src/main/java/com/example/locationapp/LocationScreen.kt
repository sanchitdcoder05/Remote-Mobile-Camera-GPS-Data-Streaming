package com.example.locationapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.location.Geocoder
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.api.Service
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocationScreen(fusedLocationClient: FusedLocationProviderClient, navController: NavController) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Press the button to get location") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationUpdates(context, fusedLocationClient) { latitude, longitude ->
                locationText = "Location: ${getAddressFromCoordinates(context, latitude, longitude)}"
                uploadLocationToFirebase(latitude, longitude, locationText)
            }
        } else {
            locationText = "Permission denied. Can't access location."
        }
    }



    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates(context, fusedLocationClient) { latitude, longitude ->
                locationText = "Latitude: $latitude, Longitude: $longitude\n" +
                        getAddressFromCoordinates(context, latitude, longitude)
                uploadLocationToFirebase(latitude, longitude, locationText)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Remote Mobile Camera & GPS Data Streaming", fontSize = 18.sp) }
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Row (
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(
                        "Targeted Device", fontSize = 25.sp,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row (
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Button(
                        modifier = Modifier.padding(10.dp),
                        onClick = {
                            navController.navigate("WebViewScreen")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text(text = "Give Permission")
                    }
                }
//              Text(text = locationText, modifier = Modifier.padding(16.dp))
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    modifier = Modifier.fillMaxWidth()
//                ){
//                    Button(
//                        onClick = {
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
//                    ) {
//                        Text(text = "Stop")
//                    }
//                }
            }
        }
    )
}

fun startLocationUpdates(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    val locationRequest = LocationRequest.create().apply {
        interval = 1000
        fastestInterval = 500
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    onLocationReceived(it.latitude, it.longitude)
                }
            }
        }, Looper.getMainLooper())
    }
}

fun getAddressFromCoordinates(
    context: Context,
    latitude: Double,
    longitude: Double
): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            "Address: ${address.getAddressLine(0)}"
        } else {
            "Unable to get address"
        }
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

fun uploadLocationToFirebase(latitude: Double, longitude: Double, address: String) {
    val database = FirebaseDatabase.getInstance()
    val locationRef = database.getReference("user").child("location")

    val locationData = LocationData(latitude, longitude, address)

    locationRef.setValue(locationData)
}



data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)
