package com.alarmsms.app.domain.usecase

import android.content.Context
import android.telephony.SmsManager
import android.os.Build
import com.alarmsms.app.domain.repository.AlarmaRepository
import com.alarmsms.app.domain.repository.ConfigRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ConfirmarRecepcionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmaRepository: AlarmaRepository,
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(alarmaId: String, numeroEmisor: String) {
        val config = configRepository.getLocal()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_uid"
        
        // 1. Build Return SMS ACK Content
        val mensajeACK = "[ACK-${config.palabraClave}] ${config.miNombre} confirmó"
        
        // 2. Dispatch return receipt SMS safely
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val partes = smsManager.divideMessage(mensajeACK)
            smsManager.sendMultipartTextMessage(numeroEmisor, null, partes, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Mark receipt confirmation in local Room database
        alarmaRepository.guardarLocal(
            com.alarmsms.app.domain.model.Alarma(
                id = alarmaId,
                emisorId = "remote",
                emisorNombre = "Emisor",
                mensaje = "Active",
                enviadaEn = System.currentTimeMillis(),
                esPropia = false,
                confirmada = true
            )
        )

        // 4. Synchronize acknowledgement state in Firestore tracker
        alarmaRepository.agregarConfirmacionRemota(
            alarmaId = alarmaId,
            userId = uid,
            timestamp = System.currentTimeMillis()
        )
    }
}
