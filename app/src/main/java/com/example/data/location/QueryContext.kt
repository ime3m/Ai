package com.example.data.location

import com.example.data.reasoning.UserIntent

/**
 * Context derived from analyzing a user's prompt and recent conversation history.
 *
 * Captures geographic targets, temporal targets, requested language, and intent.
 */
data class QueryContext(
    val requestedLocation: String? = null,
    val requestedLanguage: String? = null,
    val requestedTime: String? = null,
    val intent: UserIntent? = null,
    val isLocationExplicit: Boolean = false,
    val resolvedFromContext: Boolean = false,
    val isLocationDependentQuery: Boolean = false,
    val requiresLocationClarification: Boolean = false
)
