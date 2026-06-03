package dev.andrei.app_frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val category: String,
    val longitude: Double,
    val latitude: Double,
    val hasAccessibleFeatures: Boolean,
    val hasToilets: Boolean,
    val averageScore: Double,
    val attributes: List<String>,
    // Per-user match score (0–5) when this row came from the recommended list; null otherwise.
    val matchScore: Double? = null
)
