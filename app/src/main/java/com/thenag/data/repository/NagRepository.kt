package com.thenag.data.repository

import com.thenag.data.local.database.NagDao
import com.thenag.data.local.entity.NagItem
import com.thenag.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that provides a clean API for data operations.
 * Abstracts the data source (Room database) from the rest of the app.
 *
 * This follows the Repository pattern from Android Architecture Components.
 */
@Singleton
class NagRepository @Inject constructor(
    private val nagDao: NagDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    // Queries that return Flow automatically update observers
    fun getAllNags(): Flow<List<NagItem>> = nagDao.getAllNags()

    fun getActiveNags(): Flow<List<NagItem>> = nagDao.getActiveNags()

    fun getInactiveNags(): Flow<List<NagItem>> = nagDao.getInactiveNags()

    fun getNagsByCategory(category: String): Flow<List<NagItem>> = nagDao.getNagsByCategory(category)

    fun getNagByIdFlow(nagId: Int): Flow<NagItem?> = nagDao.getNagByIdFlow(nagId)

    fun searchNags(query: String): Flow<List<NagItem>> = nagDao.searchNags(query)

    fun getUpcomingNags(): Flow<List<NagItem>> = nagDao.getUpcomingNags()

    fun getNagCount(): Flow<Int> = nagDao.getNagCount()

    fun getActiveNagCount(): Flow<Int> = nagDao.getActiveNagCount()

    fun getTotalTriggerCount(): Flow<Int> = nagDao.getTotalTriggerCount()

    fun getAllCategories(): Flow<List<String>> = nagDao.getAllCategories()

    // Suspend functions for one-time operations
    suspend fun getNagById(nagId: Int): NagItem? = withContext(ioDispatcher) {
        nagDao.getNagById(nagId)
    }

    suspend fun insertNag(nag: NagItem): Long = withContext(ioDispatcher) {
        nagDao.insert(nag)
    }

    suspend fun insertAll(nags: List<NagItem>) = withContext(ioDispatcher) {
        nagDao.insertAll(nags)
    }

    suspend fun updateNag(nag: NagItem) = withContext(ioDispatcher) {
        nagDao.update(nag.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNag(nag: NagItem) = withContext(ioDispatcher) {
        nagDao.delete(nag)
    }

    suspend fun deleteNagById(nagId: Int) = withContext(ioDispatcher) {
        nagDao.deleteById(nagId)
    }

    suspend fun deleteAll() = withContext(ioDispatcher) {
        nagDao.deleteAll()
    }

    suspend fun toggleActive(nagId: Int) = withContext(ioDispatcher) {
        nagDao.toggleActive(nagId)
    }

    suspend fun incrementCount(nagId: Int) = withContext(ioDispatcher) {
        nagDao.incrementCount(nagId)
    }
}
