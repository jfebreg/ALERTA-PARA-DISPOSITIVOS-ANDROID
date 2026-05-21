package com.alarmsms.app.domain.repository

import com.alarmsms.app.data.local.dao.AlarmaDao
import com.alarmsms.app.data.local.entity.AlarmaEntity
import com.alarmsms.app.domain.model.Alarma
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmaRepository @Inject constructor(
    private val localDao: AlarmaDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("alarmas")

    fun getAlarmasFlow(): Flow<List<Alarma>> {
        return localDao.getAllAlarmasFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun guardarLocal(alarma: Alarma) {
        localDao.insert(AlarmaEntity.fromDomain(alarma))
    }

    suspend fun guardarRemoto(alarma: Alarma) {
        try {
            val payload = hashMapOf(
                "id" to alarma.id,
                "emisorId" to alarma.emisorId,
                "emisorNombre" to alarma.emisorNombre,
                "mensaje" to alarma.mensaje,
                "enviadaEn" to Timestamp(Date(alarma.enviadaEn)),
                "confirmaciones" to alarma.confirmaciones
            )
            collection.document(alarma.id).set(payload).await()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fire-and-forget; do not block local emergency signaling
        }
    }

    suspend fun agregarConfirmacionRemota(alarmaId: String, userId: String, timestamp: Long) {
        try {
            val docRef = collection.document(alarmaId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val confirmaciones = snapshot.get("confirmaciones") as? HashMap<String, Any> ?: hashMapOf()
                confirmaciones[userId] = timestamp
                transaction.update(docRef, "confirmaciones", confirmaciones)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
