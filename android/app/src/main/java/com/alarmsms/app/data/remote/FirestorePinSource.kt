package com.alarmsms.app.data.remote

import com.alarmsms.app.domain.model.Pin
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePinSource @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pines")

    suspend fun generarPin(pin: Pin) {
        val payload = hashMapOf(
            "pin" to pin.pin,
            "nombreDestino" to pin.nombreDestino,
            "usada" to pin.usada,
            "creadoPor" to pin.creadoPor,
            "creadoEn" to Timestamp(Date(pin.creadoEn)),
            "expiraEn" to Timestamp(Date(pin.expiraEn)),
            "usadaPor" to pin.usadaPor
        )
        // Store the PIN with the 6-digit pin value as the document ID for simple exact querying
        collection.document(pin.pin).set(payload).await()
    }

    suspend fun getPinDocument(pinCode: String): Pin? {
        val doc = collection.document(pinCode).get().await()
        if (!doc.exists()) return null
        
        val pinVal = doc.getString("pin") ?: return null
        val nombreDestino = doc.getString("nombreDestino") ?: ""
        val usada = doc.getBoolean("usada") ?: false
        val creadoPor = doc.getString("creadoPor") ?: ""
        val creadoEn = doc.getTimestamp("creadoEn")?.toDate()?.time ?: 0L
        val expiraEn = doc.getTimestamp("expiraEn")?.toDate()?.time ?: 0L
        val usadaPor = doc.getString("usadaPor")

        return Pin(pinVal, nombreDestino, usada, creadoPor, creadoEn, expiraEn, usadaPor)
    }

    suspend fun marcarPinConsumida(pinCode: String, userId: String) {
        collection.document(pinCode).update(
            "usada", true,
            "usadaPor", userId
        ).await()
    }

    suspend fun getPinesActivos(): List<Pin> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val pinVal = doc.getString("pin") ?: return@mapNotNull null
            val nombreDestino = doc.getString("nombreDestino") ?: ""
            val usada = doc.getBoolean("usada") ?: false
            val creadoPor = doc.getString("creadoPor") ?: ""
            val creadoEn = doc.getTimestamp("creadoEn")?.toDate()?.time ?: 0L
            val expiraEn = doc.getTimestamp("expiraEn")?.toDate()?.time ?: 0L
            val usadaPor = doc.getString("usadaPor")
            Pin(pinVal, nombreDestino, usada, creadoPor, creadoEn, expiraEn, usadaPor)
        }
    }

    suspend fun invalidarPin(pinCode: String) {
        // Deletes or marks it directly as consumed to block its use
        collection.document(pinCode).update("usada", true).await()
    }
}
