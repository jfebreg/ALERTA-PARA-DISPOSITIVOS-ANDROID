package com.alarmsms.app.service

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.alarmsms.app.presentation.MainActivity

class AlarmaForegroundService : Service() {

    private var smsReceiver: AlarmaSmsReceiver? = null

    override fun onCreate() {
        super.onCreate()
        registrarSmsReceiverDinamicamente()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanalNotificaciones()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sms Alarma Activa")
            .setContentText("El sistema está monitoreando señales de emergencia.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun registrarSmsReceiverDinamicamente() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            smsReceiver = AlarmaSmsReceiver()
            val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION).apply {
                priority = 999
            }
            registerReceiver(smsReceiver, filter, RECEIVER_EXPORTED)
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Canal Alarma de Sistema",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monitorea despachos de alarmas y notifica eventos"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        smsReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "alarma_sistema"
        const val NOTIFICATION_ID = 101
    }
}
