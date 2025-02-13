package com.example.locationapp

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@Composable
fun WebViewScreen(
    navController: NavController
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                //yaha change kr
                loadUrl("https://www.google.com/")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}