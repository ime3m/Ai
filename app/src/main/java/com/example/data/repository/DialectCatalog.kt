package com.example.data.repository

import com.example.data.model.RegionalDialect

object DialectCatalog {

    val keralaDistricts: List<RegionalDialect> = KeralaDialects.allKeralaDistricts
    val internationalDialects: List<RegionalDialect> = InternationalDialects.allInternationalDialects

    val dialects: List<RegionalDialect> = keralaDistricts + internationalDialects

    fun getDialectById(id: String): RegionalDialect {
        return dialects.find { it.id == id } ?: dialects.first()
    }

    fun getAllLanguages(): List<String> = dialects.map { it.language }.distinct()

    fun getCountriesForLanguage(language: String): List<String> =
        dialects.filter { it.language.equals(language, ignoreCase = true) }
            .map { it.country }.distinct()

    fun getStatesForCountry(country: String): List<String> =
        dialects.filter { it.country.equals(country, ignoreCase = true) }
            .map { it.stateOrProvince }.distinct()

    fun getStatesForLanguageAndCountry(language: String, country: String): List<String> =
        dialects.filter { 
            it.language.equals(language, ignoreCase = true) && 
            it.country.equals(country, ignoreCase = true) 
        }.map { it.stateOrProvince }.distinct()

    fun getDialectsForHierarchy(
        language: String,
        country: String,
        stateOrProvince: String
    ): List<RegionalDialect> = dialects.filter {
        it.language.equals(language, ignoreCase = true) &&
        it.country.equals(country, ignoreCase = true) &&
        it.stateOrProvince.equals(stateOrProvince, ignoreCase = true)
    }

    fun getFlagForCountry(country: String): String {
        return when (country.lowercase()) {
            "india" -> "🇮🇳"
            "united kingdom", "uk" -> "🇬🇧"
            "united states", "usa" -> "🇺🇸"
            "ireland" -> "🇮🇪"
            "australia" -> "🇦🇺"
            "kuwait" -> "🇰🇼"
            "united arab emirates", "uae" -> "🇦🇪"
            "saudi arabia" -> "🇸🇦"
            "egypt" -> "🇪🇬"
            "spain" -> "🇪🇸"
            else -> "🌐"
        }
    }

    fun getLanguageEmoji(language: String): String {
        return when (language.lowercase()) {
            "malayalam" -> "🌴"
            "english" -> "🗣️"
            "arabic" -> "🕌"
            "spanish" -> "💃"
            else -> "🌐"
        }
    }

    fun getRegionsForCountry(country: String): List<String> =
        dialects.filter { it.country.equals(country, ignoreCase = true) }
            .map { it.region }.distinct()

    fun getRegionsForState(state: String): List<String> =
        dialects.filter { it.stateOrProvince.equals(state, ignoreCase = true) }
            .map { it.region }.distinct()

    fun getCitiesForRegion(region: String): List<String> =
        dialects.filter { it.region.equals(region, ignoreCase = true) }
            .map { it.cityOrArea }.distinct()

    fun getDialectsForCity(city: String): List<RegionalDialect> =
        dialects.filter { it.cityOrArea.equals(city, ignoreCase = true) }

    fun searchDialects(query: String): List<RegionalDialect> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return dialects
        return dialects.filter {
            it.dialectName.lowercase().contains(q) ||
            it.language.lowercase().contains(q) ||
            it.cityOrArea.lowercase().contains(q) ||
            it.region.lowercase().contains(q) ||
            it.country.lowercase().contains(q) ||
            it.stateOrProvince.lowercase().contains(q) ||
            it.typicalExpressions.any { expr ->
                expr.expression.lowercase().contains(q) ||
                expr.meaning.lowercase().contains(q) ||
                expr.englishMeaning.lowercase().contains(q)
            }
        }
    }
}
