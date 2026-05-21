package com.alarmsms.app.data.local.dao

import androidx.room.*
import com.alarmsms.app.data.local.entity.AlarmaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {
    @Query("SELECT * FROM alarmas ORDER BY enviadaEn DESC")
    fun getAllAlarmasFlow(): Flow<List<AlarmaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarma: AlarmaEntity)

    @Query("UPDATE alarmas SET confirmada = :confirmada WHERE id = :alarmaId")
    suspend fun updateConfirmacion(alarmaId: String, confirmada: Boolean)

    @Query("DELETE FROM alarmas")
    suspend fun clear()
}
