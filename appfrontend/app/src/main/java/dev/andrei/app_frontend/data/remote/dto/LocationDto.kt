package dev.andrei.app_frontend.data.remote.dto

import dev.andrei.app_frontend.data.local.entity.LocationEntity
import java.util.UUID

data class LocationDto (
    val id: UUID,
    val name: String,
    val primaryCategory: String,
    val primaryCategoryDisplayName: String? = null,
    val longitude: Double,
    val latitude: Double,
    val hasAccessibleFeatures: Boolean,
    val hasToilets: Boolean,
    val averageScore: Double,
    val attributes: List<AttributeDto>,
    val matchScore: Double? = null
){
    fun toEntity() = LocationEntity(id, name, primaryCategory, primaryCategoryDisplayName, longitude, latitude, hasAccessibleFeatures, hasToilets, averageScore, attributes, matchScore)
}