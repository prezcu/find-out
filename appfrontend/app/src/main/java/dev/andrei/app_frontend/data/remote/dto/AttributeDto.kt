package dev.andrei.app_frontend.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * An attribute as sent by the API. [name] is the stable key the review API matches on;
 * [displayName] is the UI-friendly label (nullable -> client prettifies [name]).
 * @Serializable so it can also be persisted inside [LocationEntity] via Room's converter.
 */
@Serializable
data class AttributeDto(
    val name: String,
    val displayName: String? = null
)
