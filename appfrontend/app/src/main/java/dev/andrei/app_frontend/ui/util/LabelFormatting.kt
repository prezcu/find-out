package dev.andrei.app_frontend.ui.util

/**
 * Turns a raw slug/name into a UI-friendly label, used as a fallback when the backend has no
 * display_name. "amusement_park" -> "Amusement Park", "pet-friendly" -> "Pet Friendly".
 */
fun prettifyLabel(raw: String): String =
    raw.trim()
        .split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }

/** The display name when present and non-blank, otherwise a prettified fallback slug. */
fun displayLabel(displayName: String?, fallbackSlug: String): String =
    displayName?.takeIf { it.isNotBlank() } ?: prettifyLabel(fallbackSlug)
