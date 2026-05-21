package com.alarmsms.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.alarmsms.app.domain.repository.ConfigRepository
import com.alarmsms.app.domain.repository.EnroladoRepository
import com.alarmsms.app.presentation.AlarmaActivaActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmaSmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var configRepository: ConfigRepository

    @Inject
    lateinit var enroladoRepository: EnroladoRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = configRepository.getLocal()
                val enrolados = enroladoRepository.getActivosLocal()

                for (sms in msgs) {
                    val address = sms.originatingAddress ?: continue
                    val body = sms.messageBody ?: continue

                    // Check if message is a system Alarm
                    val triggerKey = "[${config.palabraClave}]"
                    val isAlarmTrigger = body.contains(triggerKey)
                    
                    // Normalise sender identity to verify validity
                    val isValidContact = enrolados.any { contact ->
                        normalizarYComparar(contact.telefono, address)
                    }

                    if (isValidContact && isAlarmTrigger) {
                        // Extract target metadata from body if present
                        val contentMsg = body.substringAfter("$triggerKey ").trim()
                        
                        // Wake screen and launch fullscreen siren overlay
                        val sirenIntent = Intent(context, AlarmaActivaActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("origen", address)
                            putExtra("mensaje", contentMsg)
                            putExtra("alarma_id", "SMS_EVENT_${System.currentTimeMillis()}")
                        }
                        context.startActivity(sirenIntent)
                        break // Single dispatch is sufficient to trigger loop
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun normalizarTelefono(numero: String, defaultPrefijo: String = "+34"): String {
            var limp = numero.replace(Regex("[\\s\\-\\(\\)]"), "")
            if (limp.startsWith("0")) {
                limp = defaultPrefijo + limp.substring(1)
            }
            if (!limp.startsWith("+")) {
                limp = defaultPrefijo + limp
            }
            return limp
        }

        fun normalizarYComparar(num1: String, num2: String): Boolean {
            val clean1 = normalizarTelefono(num1)
            val clean2 = normalizarTelefono(num2)
            
            val suffix1 = clean1.takeLast(9)
            val suffix2 = clean2.takeLast(9)
            
            return suffix1 == suffix2 && suffix1.isNotEmpty()
        }
    }
}
