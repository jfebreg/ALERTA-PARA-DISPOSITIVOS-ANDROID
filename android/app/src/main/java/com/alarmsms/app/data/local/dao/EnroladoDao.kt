package com.alarmsms.app.data.local.dao

import androidx.room.*
import com.alarmsms.app.data.local.entity.EnroladoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnroladoDao {
    @Query("SELECT * FROM enrolados ORDER BY nombre ASC")
    fun getAllEnroladosFlow(): Flow<List<EnroladoEntity>>

    @Query("SELECT * FROM enrolados ORDER BY nombre ASC")
    suspend fun getAllEnrolados(): List<EnroladoEntity>

    @Query("SELECT * FROM enrolados WHERE activo = 1")
    suspend fun getActivos(): List<EnroladoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(enrolados: List<EnroladoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(enrolado: EnroladoEntity)

    @Query("DELETE FROM enrolados WHERE id = :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM enrolados")
    suspend fun clear()
}
