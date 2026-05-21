package com.alarmsms.app.presentation

import android.app.Application
import android.content.Intent
import android.os.Build
import com.alarmsms.app.service.AlarmaForegroundService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AlarmaSmsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        iniciarServicioMonitoreoSms()
    }

    private fun iniciarServicioMonitoreoSms() {
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
