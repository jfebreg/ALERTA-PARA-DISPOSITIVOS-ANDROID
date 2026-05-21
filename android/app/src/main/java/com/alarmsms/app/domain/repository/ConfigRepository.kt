package com.alarmsms.app.domain.repository

import com.alarmsms.app.data.local.dao.ConfigDao
import com.alarmsms.app.data.local.entity.ConfigEntity
import com.alarmsms.app.domain.model.Config
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val localDao: ConfigDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val documentRef = firestore.collection("config").document("singleton")

    suspend fun getLocal(): Config {
        val local = localDao.getConfig()
        return local?.toDomain() ?: Config()
    }

    suspend fun guardarLocal(config: Config) {
        localDao.saveConfig(ConfigEntity.fromDomain(config))
    }

    suspend fun synchroniseConfigWithFirestore() {
        try {
            val doc = documentRef.get().await()
            if (doc.exists()) {
                val msg = doc.getString("mensajePredefinido") ?: "ALERTA DE SISTEMA: ¡Actúe de inmediato!"
                val kw = doc.getString("palabraClave") ?: "ALARMA"
                
                // Fetch existing identity metadata to avoid clearing local fields during sync
                val current = getLocal()
                val merged = current.copy(
                    mensajePredefinido = msg,
                    palabraClave = kw
                )
                guardarLocal(merged)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun uploadConfigToFirestore(msg: String, kw: String, ttl: Int) {
        try {
            val payload = hashMapOf(
                "mensajePredefinido" to msg,
                "palabraClave" to kw,
                "pinTtlHoras" to ttl
            )
            documentRef.set(payload).await()
            
            val current = getLocal()
            val merged = current.copy(
                mensajePredefinido = msg,
                palabraClave = kw
            )
            guardarLocal(merged)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
