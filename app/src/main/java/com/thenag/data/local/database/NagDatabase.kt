package com.thenag.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thenag.data.local.entity.NagItem

/**
 * Room database for The Nag app.
 * Holds the nag items table.
 */
@Database(
    entities = [NagItem::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NagDatabase : RoomDatabase() {

    /**
     * Provides access to Nag DAO.
     */
    abstract fun nagDao(): NagDao

    companion object {
        const val DATABASE_NAME = "nag_database"
    }
}
