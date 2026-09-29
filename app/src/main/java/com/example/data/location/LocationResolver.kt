package com.example.data.location

import com.example.data.reasoning.UniversalReasoningEngine
import com.example.data.reasoning.UserIntent
import com.example.data.regional.RegionalProfile

/**
 * Result of resolving a location from a user query and conversation context.
 */
data class ResolvedLocation(
    val location: String?,
    val source: LocationSource,
    val isExplicit: Boolean,
    val requiresClarification: Boolean = false
)

enum class LocationSource {
    EXPLICIT_QUERY,        // Mentioned directly in current prompt (e.g., "Kuwait", "Dubai", "London", "Kochi")
    CONVERSATION_CONTEXT,  // Resolved from "there" or previous turns
    USER_DEFAULT,          // User's configured/device location
    NONE                   // Location needed but unspecified
}

/**
 * LocationResolver:
 * Parses user queries to extract explicit location entities, ensuring geographic queries
 * prioritize the detected location over the regional profile settings.
 *
 * Core Principles:
 * 1. Regional Profile (e.g. Kerala) defines speech, dialect, language, and cultural warmth.
 *    It DOES NOT determine geographic location for factual queries.
 * 2. Explicit location entity in query ALWAYS takes priority (Priority 1).
 * 3. Contextual reference resolution ("there", "അവിടെ", etc.) is Priority 2.
 * 4. User's configured geographic location is Priority 3.
 * 5. Clarification is requested if location is missing for location-dependent queries.
 * 6. NEVER fallback to the regional profile setting (Kerala) as geographic location.
 */
open class LocationResolver {

    /**
     * Extracts an explicit location entity from a user query string.
     * Supports multi-lingual patterns (English, Malayalam, etc.).
     */
    fun extractLocationEntity(query: String): String? {
        val trimmed = query.trim()
        return LocationExtractor.extractExplicitLocation(trimmed, trimmed.lowercase())
    }

    /**
     * Checks if the query depends on geographic location (weather, restaurants, time, etc.).
     */
    fun isLocationDependent(query: String): Boolean {
        return LocationExtractor.isLocationDependentQuery(query.lowercase().trim())
    }

    /**
     * Resolves the target geographic location for a query, strictly adhering to the priority order.
     */
    fun resolveLocation(
        query: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        userLocation: UserLocation = UserLocation(),
        regionalProfile: RegionalProfile? = null
    ): ResolvedLocation {
        val lower = query.lowercase().trim()

        // 1. Explicit location in current user query
        val explicitLoc = extractLocationEntity(query)
        if (explicitLoc != null) {
            return ResolvedLocation(
                location = explicitLoc,
                source = LocationSource.EXPLICIT_QUERY,
                isExplicit = true,
                requiresClarification = false
            )
        }

        // 2. Contextual reference ("there", "അവിടെ", "അവിടുത്തെ", "that place")
        if (LocationExtractor.isContextualLocationReference(lower)) {
            val contextLoc = LocationExtractor.findRecentLocationInHistory(conversationHistory)
            if (contextLoc != null) {
                return ResolvedLocation(
                    location = contextLoc,
                    source = LocationSource.CONVERSATION_CONTEXT,
                    isExplicit = false,
                    requiresClarification = false
                )
            }
        }

        // 3. Device / Current location reference ("here", "ഇവിടെ", "my location")
        if (LocationExtractor.isDeviceLocationReference(lower)) {
            if (userLocation.isSpecified) {
                val loc = userLocation.city ?: userLocation.country ?: userLocation.displayName()
                return ResolvedLocation(
                    location = loc,
                    source = LocationSource.USER_DEFAULT,
                    isExplicit = false,
                    requiresClarification = false
                )
            } else {
                return ResolvedLocation(
                    location = null,
                    source = LocationSource.USER_DEFAULT,
                    isExplicit = false,
                    requiresClarification = true
                )
            }
        }

        // 4. If query is location-dependent:
        if (isLocationDependent(query)) {
            // Check user's configured location
            if (userLocation.isSpecified) {
                val loc = userLocation.city ?: userLocation.country ?: userLocation.displayName()
                return ResolvedLocation(
                    location = loc,
                    source = LocationSource.USER_DEFAULT,
                    isExplicit = false,
                    requiresClarification = false
                )
            }

            // CRITICAL: NEVER use regionalProfile (e.g. Kerala) as fallback!
            // Instead, indicate clarification is required.
            return ResolvedLocation(
                location = null,
                source = LocationSource.NONE,
                isExplicit = false,
                requiresClarification = true
            )
        }

        return ResolvedLocation(
            location = null,
            source = LocationSource.NONE,
            isExplicit = false,
            requiresClarification = false
        )
    }

    /**
     * Builds a comprehensive QueryContext.
     */
    fun resolveQueryContext(
        query: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        userLocation: UserLocation = UserLocation(),
        regionalProfile: RegionalProfile? = null
    ): QueryContext {
        val resolved = resolveLocation(query, conversationHistory, userLocation, regionalProfile)
        val intent = UniversalReasoningEngine.classifyIntent(query)
        val isLocDependent = isLocationDependent(query)

        return QueryContext(
            requestedLocation = resolved.location,
            intent = intent,
            isLocationExplicit = resolved.isExplicit,
            resolvedFromContext = resolved.source == LocationSource.CONVERSATION_CONTEXT,
            isLocationDependentQuery = isLocDependent,
            requiresLocationClarification = resolved.requiresClarification
        )
    }

    companion object : LocationResolver()
}
