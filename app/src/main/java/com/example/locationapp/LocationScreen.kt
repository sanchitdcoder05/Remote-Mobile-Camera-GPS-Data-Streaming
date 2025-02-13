package com.example.locationapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import java.util.Locale

@Composable
fun LocationScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Press the button to get location") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context, fusedLocationClient) { latitude, longitude ->
                locationText = "Latitude: $latitude, Longitude: $longitude\n" + getAddressFromCoordinates(context, latitude, longitude)
            }
        } else {
            locationText = "Permission denied"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = locationText, modifier = Modifier.padding(16.dp))
        Button(onClick = {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation(context, fusedLocationClient) { latitude, longitude ->
                    locationText = "Latitude: $latitude, Longitude: $longitude\n" + getAddressFromCoordinates(context, latitude, longitude)
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Get Location")
        }
    }
}

fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            }
        }.addOnFailureListener {
            onLocationReceived(0.0, 0.0)
        }
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