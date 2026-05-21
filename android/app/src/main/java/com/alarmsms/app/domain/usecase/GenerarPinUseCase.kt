package com.alarmsms.app.domain.usecase

import com.alarmsms.app.data.remote.FirestorePinSource
import com.alarmsms.app.domain.model.Pin
import com.google.firebase.auth.FirebaseAuth
import java.security.SecureRandom
import javax.inject.Inject

class GenerarPinUseCase @Inject constructor(
    private val pinSource: FirestorePinSource
) {
    suspend operator fun invoke(nombreDestino: String, ttlHoras: Int = 24): String {
        val pinCode = generateRandomSixDigitPin()
        val uidAdmin = FirebaseAuth.getInstance().currentUser?.uid ?: "sys"
        
        val totalTtlMs = ttlHoras.toLong() * 60L * 60L * 1000L
        val pinObj = Pin(
            pin = pinCode,
            nombreDestino = nombreDestino,
            usada = false,
            creadoPor = uidAdmin,
            creadoEn = System.currentTimeMillis(),
            expiraEn = System.currentTimeMillis() + totalTtlMs
        )
        
        pinSource.generarPin(pinObj)
        return pinCode
    }

    private fun generateRandomSixDigitPin(): String {
        val random = SecureRandom()
        val num = random.nextInt(900000) + 100000
        return num.toString()
    }
}
