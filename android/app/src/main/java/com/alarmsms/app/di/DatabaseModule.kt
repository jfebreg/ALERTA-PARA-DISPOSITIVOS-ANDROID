package com.alarmsms.app.di

import android.content.Context
import androidx.room.Room
import com.alarmsms.app.data.local.AppDatabase
import com.alarmsms.app.data.local.dao.AlarmaDao
import com.alarmsms.app.data.local.dao.ConfigDao
import com.alarmsms.app.data.local.dao.EnroladoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "alarma_sms_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideEnroladoDao(database: AppDatabase): EnroladoDao {
        return database.enroladoDao()
    }

    @Provides
    fun provideAlarmaDao(database: AppDatabase): AlarmaDao {
        return database.alarmaDao()
    }

    @Provides
    fun provideConfigDao(database: AppDatabase): ConfigDao {
        return database.configDao()
    }
}
