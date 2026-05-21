package com.alarmsms.app.domain.usecase

import com.alarmsms.app.data.remote.FirestorePinSource
import com.alarmsms.app.domain.model.Pin
import com.alarmsms.app.domain.model.Enrolado
import com.alarmsms.app.domain.repository.EnroladoRepository
import com.alarmsms.app.domain.repository.ConfigRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class ValidarPinUseCase @Inject constructor(
    private val pinSource: FirestorePinSource,
    private val enroladoRepo: EnroladoRepository,
    private val configRepo: ConfigRepository
) {
    sealed class Result {
        data class Success(val pin: Pin) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(telefonoRaw: String, pinCode: String): Result {
        try {
            val pinObj = pinSource.getPinDocument(pinCode) 
                ?: return Result.Error("El PIN ingresado no existe o es inválido.")

            if (pinObj.usada) {
                return Result.Error("Este PIN ya ha sido utilizado por otro dispositivo.")
            }

            if (System.currentTimeMillis() > pinObj.expiraEn) {
                return Result.Error("El PIN ha vencido (superó el tiempo límite). Solicite uno nuevo.")
            }

            // Normalise and confirm phone connection
            val auth = FirebaseAuth.getInstance()
            
            // For custom app logic with anonymous login representing a registered phone user
            val currentUser = auth.currentUser ?: auth.signInAnonymously().awaitResult()
            val uid = currentUser?.uid ?: return Result.Error("Error al iniciar sesión segura en Firebase.")

            // Mark PIN as consumed
            pinSource.marcarPinConsumida(pinCode, uid)

            // Register as Enrolado in Firestore /enrolados/{uid}
            val nuevoEnrolado = Enrolado(
                id = uid,
                nombre = pinObj.nombreDestino,
                telefono = telefonoRaw,
                rol = "USER",
                activo = true,
                creadoEn = System.currentTimeMillis(),
                ultimaSync = System.currentTimeMillis()
            )
            enroladoRepo.registrarNuevoEnrolado(nuevoEnrolado)

            // Save local profile config
            val initialConfig = configRepo.getLocal().copy(
                miNombre = pinObj.nombreDestino,
                miTelefono = telefonoRaw,
                miRol = "USER"
            )
            configRepo.guardarLocal(initialConfig)

            return Result.Success(pinObj)
        } catch (e: Exception) {
            return Result.Error("Excepción de red: ${e.localizedMessage ?: "Consulte conexión"}")
        }
    }

    // Helper extension extension to make firebase awaitable cleanly
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T? {
        return try {
            kotlinx.coroutines.tasks.await()
        } catch (e: Exception) {
            null
        }
    }
}
