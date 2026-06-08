package dev.andrei.app_frontend.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * An attribute as sent by the API. [name] is the stable key the review API matches on;
 * [displayName] is the UI-friendly label (nullable -> client prettifies [name]); [conceptSlug] is
 * the slug of the mapped attribute concept (nullable), used to join the user's per-concept weight in
 * the attraction ledger; [averageScore] is this location attribute's running average score.
 * Defaults keep older Room-cached JSON (written before these fields existed) decodable.
 * @Serializable so it can also be persisted inside [LocationEntity] via Room's converter.
 */
@Serializable
data class AttributeDto(
    val name: String,
    val displayName: String? = null,
    val conceptSlug: String? = null,
    val averageScore: Double = 0.0
)
