package com.alarmsms.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alarmsms.app.data.local.dao.AlarmaDao
import com.alarmsms.app.data.local.dao.ConfigDao
import com.alarmsms.app.data.local.dao.EnroladoDao
import com.alarmsms.app.data.local.entity.AlarmaEntity
import com.alarmsms.app.data.local.entity.ConfigEntity
import com.alarmsms.app.data.local.entity.EnroladoEntity

@Database(
    entities = [EnroladoEntity::class, AlarmaEntity::class, ConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun enroladoDao(): EnroladoDao
    abstract fun alarmaDao(): AlarmaDao
    abstract fun configDao(): ConfigDao
}
