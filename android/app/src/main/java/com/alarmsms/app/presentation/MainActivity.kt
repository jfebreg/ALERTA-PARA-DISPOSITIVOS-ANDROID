package com.alarmsms.app.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alarmsms.app.presentation.navigation.AppNavGraph
import com.alarmsms.app.service.AlarmaForegroundService
import com.alarmsms.app.ui.theme.AlarmaSmsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kickstartSmsService()

        setContent {
            AlarmaSmsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestRuntimePermissionsAndLaunchApp()
                }
            }
        }
    }

    private fun kickstartSmsService() {
        val serviceIntent = Intent(this, AlarmaForegroundService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestRuntimePermissionsAndLaunchApp() {
    // Collect target system permission scopes
    val permissionsToRequest = mutableListOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.CAMERA,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.VIBRATE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionsState.allPermissionsGranted) {
        // Safe execution when clearances are validated
        AppNavGraph()
    } else {
        // Fallback interface to explain missing clearance hooks
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Permisos Requeridos",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Esta aplicación requiere permisos de procesamiento de SMS para actuar ante despachos de sirens, " +
                           "vocalizar alarmas, parpadear luz y vibrar en bloqueos. Por favor autorícelos en la configuración de Android.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() }
                ) {
                    Text("Reintentar Autorización")
                }
            }
        }
    }
}
