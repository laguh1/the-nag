package com.thenag.data.local.database

import androidx.room.*
import com.thenag.data.local.entity.NagItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Nag items.
 * Defines database operations for the nag_items table.
 */
@Dao
interface NagDao {

    /**
     * Get all nags ordered by creation date (newest first).
     * Returns a Flow that emits whenever the data changes.
     */
    @Query("SELECT * FROM nag_items ORDER BY createdAt DESC")
    fun getAllNags(): Flow<List<NagItem>>

    /**
     * Get all active nags ordered by scheduled time.
     */
    @Query("SELECT * FROM nag_items WHERE isActive = 1 ORDER BY scheduledTimestamp ASC")
    fun getActiveNags(): Flow<List<NagItem>>

    /**
     * Get all inactive nags.
     */
    @Query("SELECT * FROM nag_items WHERE isActive = 0 ORDER BY createdAt DESC")
    fun getInactiveNags(): Flow<List<NagItem>>

    /**
     * Get nags by category/event type.
     */
    @Query("SELECT * FROM nag_items WHERE event = :category ORDER BY createdAt DESC")
    fun getNagsByCategory(category: String): Flow<List<NagItem>>

    /**
     * Get a single nag by ID.
     * Returns null if not found.
     */
    @Query("SELECT * FROM nag_items WHERE id = :nagId")
    suspend fun getNagById(nagId: Int): NagItem?

    /**
     * Get a single nag by ID as Flow (for observing changes).
     */
    @Query("SELECT * FROM nag_items WHERE id = :nagId")
    fun getNagByIdFlow(nagId: Int): Flow<NagItem?>

    /**
     * Search nags by name or category.
     */
    @Query("""
        SELECT * FROM nag_items
        WHERE name LIKE '%' || :query || '%'
        OR event LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchNags(query: String): Flow<List<NagItem>>

    /**
     * Get upcoming nags (scheduled in the future).
     */
    @Query("""
        SELECT * FROM nag_items
        WHERE isActive = 1
        AND scheduledTimestamp > :currentTime
        ORDER BY scheduledTimestamp ASC
    """)
    fun getUpcomingNags(currentTime: Long = System.currentTimeMillis()): Flow<List<NagItem>>

    /**
     * Insert a new nag.
     * Returns the row ID of the inserted nag.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nag: NagItem): Long

    /**
     * Insert multiple nags.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nags: List<NagItem>)

    /**
     * Update an existing nag.
     */
    @Update
    suspend fun update(nag: NagItem)

    /**
     * Delete a nag.
     */
    @Delete
    suspend fun delete(nag: NagItem)

    /**
     * Delete a nag by ID.
     */
    @Query("DELETE FROM nag_items WHERE id = :nagId")
    suspend fun deleteById(nagId: Int)

    /**
     * Delete all nags.
     */
    @Query("DELETE FROM nag_items")
    suspend fun deleteAll()

    /**
     * Get count of all nags.
     */
    @Query("SELECT COUNT(*) FROM nag_items")
    fun getNagCount(): Flow<Int>

    /**
     * Get count of active nags.
     */
    @Query("SELECT COUNT(*) FROM nag_items WHERE isActive = 1")
    fun getActiveNagCount(): Flow<Int>

    /**
     * Get total trigger count across all nags.
     */
    @Query("SELECT SUM(count) FROM nag_items")
    fun getTotalTriggerCount(): Flow<Int>

    /**
     * Get all unique categories.
     */
    @Query("SELECT DISTINCT event FROM nag_items WHERE event IS NOT NULL AND event != ''")
    fun getAllCategories(): Flow<List<String>>

    /**
     * Toggle nag active status.
     */
    @Query("UPDATE nag_items SET isActive = NOT isActive, updatedAt = :timestamp WHERE id = :nagId")
    suspend fun toggleActive(nagId: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Increment trigger count for a nag.
     */
    @Query("UPDATE nag_items SET count = count + 1, updatedAt = :timestamp WHERE id = :nagId")
    suspend fun incrementCount(nagId: Int, timestamp: Long = System.currentTimeMillis())
}
