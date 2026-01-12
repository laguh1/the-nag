package com.thenag.data.local.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room database.
 * Handles conversion between complex types and primitive types that SQLite can store.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Convert List<String> to JSON string for database storage.
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    /**
     * Convert JSON string from database to List<String>.
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
