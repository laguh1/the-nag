package com.thenag.di

import android.content.Context
import androidx.room.Room
import com.thenag.data.local.database.NagDao
import com.thenag.data.local.database.NagDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     * Singleton ensures only one database instance exists throughout the app lifecycle.
     */
    @Provides
    @Singleton
    fun provideNagDatabase(
        @ApplicationContext context: Context
    ): NagDatabase {
        return Room.databaseBuilder(
            context,
            NagDatabase::class.java,
            NagDatabase.DATABASE_NAME
        )
            // For production, remove this and handle migrations properly
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the Nag DAO from the database.
     */
    @Provides
    @Singleton
    fun provideNagDao(database: NagDatabase): NagDao {
        return database.nagDao()
    }
}
