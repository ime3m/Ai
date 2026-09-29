package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

import com.example.data.location.UserLocation

/**
 * Local persistent store for user settings:
 * - Selected Region ID (default: "kerala") - CONTROLS LANGUAGE & REGIONAL CONVERSATIONAL STYLE ONLY
 * - Selected Language code (default: "ml")
 * - Selected Voice/Dialect ID (default: "ml_in_kl_kozhikode")
 * - User Geographic Location (country, city, lat, lon) - CONTROLS DEFAULT GEOGRAPHY ONLY
 * - First launch welcome completion flag (default: false)
 *
 * NOTE: Regional Profile and User Geographic Location are strictly separated!
 */
class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasCompletedFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETED, value).apply()

    var selectedRegionId: String
        get() = prefs.getString(KEY_SELECTED_REGION, DEFAULT_REGION_ID) ?: DEFAULT_REGION_ID
        set(value) = prefs.edit().putString(KEY_SELECTED_REGION, value).apply()

    var selectedLanguage: String
        get() = prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        set(value) = prefs.edit().putString(KEY_SELECTED_LANGUAGE, value).apply()

    var selectedVoiceId: String
        get() = prefs.getString(KEY_SELECTED_VOICE, DEFAULT_VOICE_ID) ?: DEFAULT_VOICE_ID
        set(value) = prefs.edit().putString(KEY_SELECTED_VOICE, value).apply()

    // Separate User Geographic Location
    var userLocationCity: String?
        get() = prefs.getString(KEY_USER_LOCATION_CITY, null)
        set(value) = prefs.edit().putString(KEY_USER_LOCATION_CITY, value).apply()

    var userLocationCountry: String?
        get() = prefs.getString(KEY_USER_LOCATION_COUNTRY, null)
        set(value) = prefs.edit().putString(KEY_USER_LOCATION_COUNTRY, value).apply()

    var userLocationSource: String
        get() = prefs.getString(KEY_USER_LOCATION_SOURCE, UserLocation.SOURCE_UNSPECIFIED) ?: UserLocation.SOURCE_UNSPECIFIED
        set(value) = prefs.edit().putString(KEY_USER_LOCATION_SOURCE, value).apply()

    fun getUserLocation(): UserLocation {
        return UserLocation(
            country = userLocationCountry,
            city = userLocationCity,
            source = userLocationSource
        )
    }

    fun saveUserLocation(location: UserLocation) {
        userLocationCity = location.city
        userLocationCountry = location.country
        userLocationSource = location.source
    }

    companion object {
        private const val PREFS_NAME = "regional_voice_ai_user_prefs"
        private const val KEY_FIRST_LAUNCH_COMPLETED = "has_completed_first_launch"
        private const val KEY_SELECTED_REGION = "selected_region_id"
        private const val KEY_SELECTED_LANGUAGE = "selected_language_code"
        private const val KEY_SELECTED_VOICE = "selected_voice_id"
        private const val KEY_USER_LOCATION_CITY = "user_location_city"
        private const val KEY_USER_LOCATION_COUNTRY = "user_location_country"
        private const val KEY_USER_LOCATION_SOURCE = "user_location_source"

        const val DEFAULT_REGION_ID = "kerala"
        const val DEFAULT_LANGUAGE = "ml"
        const val DEFAULT_VOICE_ID = "ml_in_kl_kozhikode"
    }
}
