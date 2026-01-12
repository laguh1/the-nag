package com.thenag.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a Nag item in the database.
 * This matches the original TinyDB structure from the MIT App Inventor version.
 */
@Entity(tableName = "nag_items")
data class NagItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Basic information
    val name: String,
    val event: String,              // Category/type
    val timeRange: Int,             // Duration in minutes

    // Tracking
    val count: Int = 0,             // Number of times triggered
    val isActive: Boolean = true,   // Is this nag active?
    val isDateSet: Boolean = false, // Has a date been set?

    // Date/Time components (preserved from original structure)
    val year: Int? = null,
    val month: Int? = null,          // 1-12
    val weekInYear: Int? = null,
    val dayInYear: Int? = null,
    val hour: Int? = null,           // 0-23
    val minute: Int? = null,         // 0-59

    // Computed timestamp for easier querying and scheduling
    val scheduledTimestamp: Long? = null,

    // Notification messages
    val completeMessages: List<String> = emptyList(),

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
