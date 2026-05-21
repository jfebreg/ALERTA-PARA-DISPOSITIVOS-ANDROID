package com.alarmsms.app.data.remote

import com.alarmsms.app.domain.model.Enrolado
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreEnroladoSource @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("enrolados")

    suspend fun getEnrolados(): List<Enrolado> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val nombre = doc.getString("nombre") ?: return@mapNotNull null
            val telefono = doc.getString("telefono") ?: return@mapNotNull null
            val rol = doc.getString("rol") ?: "USER"
            val activo = doc.getBoolean("activo") ?: true
            val creadoEn = doc.getTimestamp("creadoEn")?.toDate()?.time ?: 0L
            val ultimaSync = doc.getTimestamp("ultimaSync")?.toDate()?.time ?: 0L
            Enrolado(id, nombre, telefono, rol, activo, creadoEn, ultimaSync)
        }
    }

    suspend fun registrarEnrolado(enrolado: Enrolado) {
        val payload = hashMapOf(
            "nombre" to enrolado.nombre,
            "telefono" to enrolado.telefono,
            "rol" to enrolado.rol,
            "activo" to enrolado.activo,
            "creadoEn" to com.google.firebase.Timestamp(Date(enrolado.creadoEn)),
            "ultimaSync" to com.google.firebase.Timestamp(Date(enrolado.ultimaSync))
        )
        collection.document(enrolado.id).set(payload).await()
    }

    suspend fun updateActivo(userId: String, activo: Boolean) {
        collection.document(userId).update("activo", activo).await()
    }

    suspend fun eliminarEnrolado(userId: String) {
        collection.document(userId).delete().await()
    }
}
