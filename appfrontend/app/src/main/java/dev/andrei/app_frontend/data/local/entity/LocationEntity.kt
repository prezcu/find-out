package dev.andrei.app_frontend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.andrei.app_frontend.data.remote.dto.AttributeDto
import java.util.UUID

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val category: String,
    // UI-friendly category label; null -> client prettifies [category].
    val primaryCategoryDisplayName: String? = null,
    val longitude: Double,
    val latitude: Double,
    val hasAccessibleFeatures: Boolean,
    val hasToilets: Boolean,
    val averageScore: Double,
    val attributes: List<AttributeDto>,
    // Per-user match score (0–5) when this row came from the recommended list; null otherwise.
    val matchScore: Double? = null
)
