package com.alarmsms.app.presentation

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alarmsms.app.domain.usecase.ConfirmarRecepcionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AlarmaActivaActivity : ComponentActivity() {

    @Inject
    lateinit var confirmarRecepcionUseCase: ConfirmarRecepcionUseCase

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var strobeState = false

    private val strobeRunnable = object : Runnable {
        override fun run() {
            try {
                cameraId?.let { id ->
                    strobeState = !strobeState
                    cameraManager?.setTorchMode(id, strobeState)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mantenVentanaEncendidaYDespierta()
        adquirirWakeLock()
        reproducirAlarmaAudio()
        dispararVibracion()
        iniciarStrobeFlashlight()

        val origen = intent.getStringExtra("origen") ?: "Contacto Enrolado"
        val mensaje = intent.getStringExtra("mensaje") ?: "Mensaje de pánico predefinido."
        val alarmaId = intent.getStringExtra("alarma_id") ?: ""

        setContent {
            SirenAlertFullscreen(
                origen = origen,
                mensaje = mensaje,
                onConfirm = {
                    detenerSirenaVisual()
                    CoroutineScope(Dispatchers.Main).launch {
                        confirmarRecepcionUseCase(alarmaId, origen)
                        finish()
                    }
                }
            )
        }
    }

    private fun mantenVentanaEncendidaYDespierta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun adquirirWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SmsAlarma::EmergencySirenWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes limit
        }
    }

    private fun reproducirAlarmaAudio() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            mediaPlayer = MediaPlayer().apply {
                val rawResourceId = resources.getIdentifier("alarm", "raw", packageName)
                if (rawResourceId != 0) {
                    val afd = resources.openRawResourceFd(rawResourceId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else {
                    // Failover URI to global device alarm siren if custom raw res isn't bundled yet
                    val alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    setDataSource(applicationContext, alarmUri ?: Uri.parse("content://settings/system/alarm_alert"))
                }
                
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dispararVibracion() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun iniciarStrobeFlashlight() {
        try {
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                handler.post(strobeRunnable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun detenerSirenaVisual() {
        handler.removeCallbacks(strobeRunnable)
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onDestroy() {
        detenerSirenaVisual()
        super.onDestroy()
    }
}

@Composable
fun SirenAlertFullscreen(
    origen: String,
    mensaje: String,
    onConfirm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8C0000))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Red flashing background effect indicator
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulseScale)
                .background(Color(0xFFFF3B30), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚨",
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "ALERTA SMS RECIBIDA",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DE: $origen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFCC00),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = mensaje,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val curTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Text(
            text = "Recibida hace un instante - $curTime",
            color = Color(0xAAFFFFFF),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF8C0000)
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(64.dp),
            shape = CircleShape
        ) {
            Text(
                text = "CONFIRMAR RECEPCIÓN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
