package dev.andrei.app_frontend.data.remote.dto

import dev.andrei.app_frontend.data.local.entity.LocationEntity
import java.util.UUID

data class LocationDto (
    val id: UUID,
    val name: String,
    val primaryCategory: String,
    val longitude: Double,
    val latitude: Double,
    val hasAccessibleFeatures: Boolean,
    val hasToilets: Boolean,
    val averageScore: Double
){
    fun toEntity() = LocationEntity(id, name, primaryCategory, longitude, latitude, hasAccessibleFeatures, hasToilets, averageScore)
}