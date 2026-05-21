package com.alarmsms.app.domain.usecase

import android.content.Context
import android.telephony.SmsManager
import android.os.Build
import com.alarmsms.app.domain.model.Alarma
import com.alarmsms.app.domain.repository.AlarmaRepository
import com.alarmsms.app.domain.repository.ConfigRepository
import com.alarmsms.app.domain.repository.EnroladoRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class EmitirAlarmaUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enroladoRepository: EnroladoRepository,
    private val alarmaRepository: AlarmaRepository,
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(mensajeCustom: String? = null): String {
        val config = configRepository.getLocal()
        val enrolados = enroladoRepository.getActivosLocal()
        
        // Build payload string containing system trigger keyword
        val contenidoAlarma = "[${config.palabraClave}] ${config.miNombre}: ${mensajeCustom ?: config.mensajePredefinido}"
        
        // Build SmsManager instance safely
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        // Loop and dispatch to all active system connections
        enrolados.forEach { enrolado ->
            val partes = smsManager.divideMessage(contenidoAlarma)
            smsManager.sendMultipartTextMessage(enrolado.telefono, null, partes, null, null)
        }

        // Persist alarm trigger event locally & synchronously
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_uid"
        val alarmaObj = Alarma(
            id = UUID.randomUUID().toString(),
            emisorId = uid,
            emisorNombre = config.miNombre,
            mensaje = mensajeCustom ?: config.mensajePredefinido,
            enviadaEn = System.currentTimeMillis(),
            esPropia = true,
            confirmada = true
        )
        
        alarmaRepository.guardarLocal(alarmaObj)
        alarmaRepository.guardarRemoto(alarmaObj) // fire and forget to synchronise live dashboards
        return alarmaObj.id
    }
}
