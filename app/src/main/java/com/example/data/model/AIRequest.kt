package com.example.data.model

import com.example.data.location.QueryContext
import com.example.data.location.UserLocation
import com.example.data.regional.RegionalProfile

/**
 * Encapsulates a complete AI request separating regional conversational style from geographic location.
 *
 * - regionalProfile: Controls voice, language, dialect, pronunciation, and conversational style.
 * - queryLocation: Explicit or contextually-resolved geographic location for factual queries.
 * - userLocation: Current/default geographic location of the user (independent of regional profile).
 */
data class AIRequest(
    val message: String,
    val regionalProfile: RegionalProfile,
    val queryLocation: String? = null,
    val userLocation: UserLocation = UserLocation(),
    val queryContext: QueryContext? = null,
    val conversationHistory: List<Pair<String, String>> = emptyList()
)
