package com.alarmsms.app.data.local.dao

import androidx.room.*
import com.alarmsms.app.data.local.entity.ConfigEntity

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configs WHERE id = 'singleton' LIMIT 1")
    suspend fun getConfig(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfigEntity)

    @Query("DELETE FROM configs")
    suspend fun clear()
}
