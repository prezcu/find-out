package dev.andrei.app_frontend.ui.util

/**
 * The curated subset of attribute concepts asked during first-run onboarding — kept short on
 * purpose so a brand-new user sets a few high-signal tastes without facing all 23. Everything
 * else is tuned later on the full Preferences screen.
 *
 * These are concept *slugs* (stable keys from `attribute_concept.slug`), in the order they're
 * asked. Unknown slugs are simply skipped, so this list degrades gracefully if the catalogue
 * changes server-side.
 */
val ONBOARDING_SLUGS: List<String> = listOf(
    "atmosphere",
    "liveliness",
    "food_quality",
    "value_for_money",
    "cleanliness",
    "service",
)
