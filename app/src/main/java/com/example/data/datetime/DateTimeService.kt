package com.example.data.datetime

import com.example.data.model.RegionalDialect
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * System Clock & Date/Time Service.
 *
 * Guarantees:
 * 1. Date and time ALWAYS come from the device/system clock (or resolved city timezone).
 * 2. NEVER hard-codes dates (e.g. "September 24, 2026" or "September 25, 2026").
 * 3. Supports Malayalam and English formatting for "today", "tomorrow", "now".
 */
object DateTimeService {

    private val MALAYALAM_MONTHS = arrayOf(
        "", "ജനുവരി", "ഫെബ്രുവരി", "മാർച്ച്", "ഏപ്രിൽ", "മെയ്", "ജൂൺ",
        "ജൂലൈ", "ആഗസ്റ്റ്", "സെപ്റ്റംബർ", "ഒക്ടോബർ", "നവംബർ", "ഡിസംബർ"
    )

    private val MALAYALAM_DAYS = mapOf(
        java.time.DayOfWeek.MONDAY to "തിങ്കളാഴ്ച",
        java.time.DayOfWeek.TUESDAY to "ചൊവ്വാഴ്ച",
        java.time.DayOfWeek.WEDNESDAY to "ബുധനാഴ്ച",
        java.time.DayOfWeek.THURSDAY to "വ്യാഴാഴ്ച",
        java.time.DayOfWeek.FRIDAY to "വെള്ളിയാഴ്ച",
        java.time.DayOfWeek.SATURDAY to "ശനിയാഴ്ച",
        java.time.DayOfWeek.SUNDAY to "ഞായറാഴ്ച"
    )

    fun getCurrentDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return LocalDate.now(zoneId)
    }

    fun getCurrentTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalTime {
        return LocalTime.now(zoneId)
    }

    fun getCurrentDateTime(zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
        return ZonedDateTime.now(zoneId)
    }

    fun getTomorrowDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return LocalDate.now(zoneId).plusDays(1)
    }

    /**
     * Formats current date in English (e.g. "September 27, 2026").
     */
    fun formatCurrentDateEnglish(date: LocalDate = LocalDate.now()): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
        return date.format(formatter)
    }

    /**
     * Formats current date with day in English (e.g. "Sunday, September 27, 2026").
     */
    fun formatFullDateEnglish(date: LocalDate = LocalDate.now()): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
        return date.format(formatter)
    }

    /**
     * Formats date in Malayalam (e.g. "2026 സെപ്റ്റംബർ 27, ഞായറാഴ്ച").
     */
    fun formatDateMalayalam(date: LocalDate = LocalDate.now()): String {
        val year = date.year
        val monthName = MALAYALAM_MONTHS.getOrElse(date.monthValue) { date.month.name }
        val dayOfMonth = date.dayOfMonth
        val dayOfWeek = MALAYALAM_DAYS[date.dayOfWeek] ?: date.dayOfWeek.name
        return "$year $monthName $dayOfMonth, $dayOfWeek"
    }

    /**
     * Checks if a query is asking for today's date or day.
     */
    fun isDateQuery(query: String): Boolean {
        val lower = query.lowercase().trim()
        return lower.contains("തീയതി") ||
                lower.contains("തിയതി") ||
                lower.contains("ഇന്ന് എത്ര തീയതി") ||
                lower.contains("ഇന്നത്തെ തീയതി") ||
                lower.contains("തീയതി എത്ര") ||
                lower.contains("തിയതി എത്ര") ||
                lower.contains("ഇന്ന് എന്ത് ദിവസം") ||
                lower.contains("ഇന്ന് എന്താ ദിവസം") ||
                lower.contains("what is today's date") ||
                lower.contains("what's today's date") ||
                lower.contains("what is the date") ||
                lower.contains("what date is it") ||
                lower.contains("today's date") ||
                lower.contains("todays date") ||
                lower.contains("current date") ||
                lower.contains("date today")
    }

    /**
     * Checks if a query is asking for the current time.
     */
    fun isTimeQuery(query: String): Boolean {
        val lower = query.lowercase().trim()
        return lower.contains("സമയം എത്ര") ||
                lower.contains("സമയം എന്തായി") ||
                lower.contains("ഇപ്പോൾ സമയം") ||
                lower.contains("ഇപ്പോഴത്തെ സമയം") ||
                lower.contains("time in") ||
                lower.contains("what is the time") ||
                lower.contains("what's the time") ||
                lower.contains("what time is it") ||
                lower.contains("current time") ||
                lower.contains("time now")
    }

    /**
     * Generates a date response based on current system clock.
     */
    fun formatCurrentDateResponse(query: String, dialect: RegionalDialect): String {
        val lower = query.lowercase().trim()
        val isTomorrow = lower.contains("നാളെ") || lower.contains("tomorrow")
        val isMalayalam = dialect.language.equals("Malayalam", true)

        val targetDate = if (isTomorrow) getTomorrowDate() else getCurrentDate()

        return if (isMalayalam) {
            val formatted = formatDateMalayalam(targetDate)
            if (isTomorrow) {
                "നാളെ $formatted ആണ്."
            } else {
                "ഇന്ന് $formatted ആണ്."
            }
        } else {
            val formatted = formatFullDateEnglish(targetDate)
            if (isTomorrow) {
                "Tomorrow is $formatted."
            } else {
                "Today is $formatted."
            }
        }
    }

    /**
     * Resolves timezone for common cities or defaults to system default.
     */
    fun resolveZoneId(locationName: String?): ZoneId {
        if (locationName.isNullOrBlank()) return ZoneId.systemDefault()
        return when (locationName.lowercase().trim()) {
            "kuwait", "kuwait city", "farwaniya", "hawalli" -> ZoneId.of("Asia/Kuwait")
            "dubai", "abu dhabi", "sharjah", "uae" -> ZoneId.of("Asia/Dubai")
            "doha", "qatar" -> ZoneId.of("Asia/Qatar")
            "riyadh", "jeddah", "saudi arabia" -> ZoneId.of("Asia/Riyadh")
            "bahrain", "manama" -> ZoneId.of("Asia/Bahrain")
            "muscat", "oman" -> ZoneId.of("Asia/Muscat")
            "london", "uk", "united kingdom" -> ZoneId.of("Europe/London")
            "tokyo", "japan" -> ZoneId.of("Asia/Tokyo")
            "paris", "france", "berlin", "germany" -> ZoneId.of("Europe/Paris")
            "new york", "nyc", "usa", "united states" -> ZoneId.of("America/New_York")
            "chicago" -> ZoneId.of("America/Chicago")
            "los angeles", "san francisco" -> ZoneId.of("America/Los_Angeles")
            "singapore" -> ZoneId.of("Asia/Singapore")
            "sydney", "australia" -> ZoneId.of("Australia/Sydney")
            "kochi", "kozhikode", "calicut", "kerala", "delhi", "mumbai", "bangalore", "chennai", "india" -> ZoneId.of("Asia/Kolkata")
            else -> ZoneId.systemDefault()
        }
    }

    /**
     * Formats a message timestamp into user's local time (e.g. "4:32 AM" or "16:32").
     * Automatically adapts to device's local timezone and 12-hour/24-hour preference.
     * Gracefully returns empty string if timestamp is invalid (<= 0).
     */
    fun formatMessageTime(context: android.content.Context, timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return try {
            val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
            val pattern = if (is24Hour) "HH:mm" else "h:mm a"
            val sdf = java.text.SimpleDateFormat(pattern, Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getDefault()
            sdf.format(java.util.Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
