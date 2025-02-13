package com.example.locationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {



            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "LocationScreen") {
                composable("LocationScreen") {
                    LocationScreen(fusedLocationClient, navController)
                }
                composable("WebViewScreen") {
                    WebViewScreen(navController)
                }
            }

        }
    }
}
