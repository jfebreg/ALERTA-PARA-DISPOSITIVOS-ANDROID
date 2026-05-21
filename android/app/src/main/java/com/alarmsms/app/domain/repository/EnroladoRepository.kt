package com.alarmsms.app.domain.repository

import com.alarmsms.app.data.local.dao.EnroladoDao
import com.alarmsms.app.data.local.entity.EnroladoEntity
import com.alarmsms.app.data.remote.FirestoreEnroladoSource
import com.alarmsms.app.domain.model.Enrolado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnroladoRepository @Inject constructor(
    private val localDao: EnroladoDao,
    private val remoteSource: FirestoreEnroladoSource
) {
    fun getEnroladosLocalFlow(): Flow<List<Enrolado>> {
        return localDao.getAllEnroladosFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getActivosLocal(): List<Enrolado> {
        return localDao.getActivos().map { it.toDomain() }
    }

    suspend fun synchroniseWithFirestore() {
        try {
            val remoteList = remoteSource.getEnrolados()
            val entities = remoteList.map { EnroladoEntity.fromDomain(it) }
            localDao.clear()
            localDao.insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
            // Maintain Room local records upon failures
        }
    }

    suspend fun registrarNuevoEnrolado(enrolado: Enrolado) {
        // Enforce Firestore persistence and subsequently Room sync
        remoteSource.registrarEnrolado(enrolado)
        localDao.insert(EnroladoEntity.fromDomain(enrolado))
    }

    suspend fun logicToggleActivo(userId: String, activo: Boolean) {
        remoteSource.updateActivo(userId, activo)
        synchroniseWithFirestore()
    }

    suspend fun logicEliminar(userId: String) {
        remoteSource.eliminarEnrolado(userId)
        localDao.deleteById(userId)
    }
}
