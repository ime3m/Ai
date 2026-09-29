package com.example.data.location

/**
 * Represents the user's geographic location.
 *
 * NOTE: UserLocation is purely geographic and is INDEPENDENT of RegionalProfile
 * (which controls language, dialect, pronunciation, voice, and conversational style).
 */
data class UserLocation(
    val country: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val source: String = SOURCE_UNSPECIFIED // "USER_CONFIGURED", "DEVICE_GPS", "DEFAULT", "UNSPECIFIED"
) {
    val isSpecified: Boolean
        get() = !city.isNullOrBlank() || !country.isNullOrBlank() || (latitude != null && longitude != null)

    fun displayName(): String {
        return when {
            !city.isNullOrBlank() && !country.isNullOrBlank() -> "$city, $country"
            !city.isNullOrBlank() -> city
            !country.isNullOrBlank() -> country
            else -> "Unspecified Location"
        }
    }

    companion object {
        const val SOURCE_USER_CONFIGURED = "USER_CONFIGURED"
        const val SOURCE_DEVICE_GPS = "DEVICE_GPS"
        const val SOURCE_DEFAULT = "DEFAULT"
        const val SOURCE_UNSPECIFIED = "UNSPECIFIED"
    }
}
