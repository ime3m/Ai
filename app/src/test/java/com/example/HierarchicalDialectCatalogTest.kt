package com.example

import com.example.data.repository.DialectCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HierarchicalDialectCatalogTest {

    @Test
    fun testLanguageToCountryDrillDown() {
        val languages = DialectCatalog.getAllLanguages()
        assertTrue("Expected Malayalam in languages", languages.contains("Malayalam"))
        assertTrue("Expected English in languages", languages.contains("English"))
        assertTrue("Expected Arabic in languages", languages.contains("Arabic"))
        assertTrue("Expected Spanish in languages", languages.contains("Spanish"))

        // Level 2: Countries for Malayalam
        val malayalamCountries = DialectCatalog.getCountriesForLanguage("Malayalam")
        assertEquals(listOf("India"), malayalamCountries)

        // Level 2: Countries for English
        val englishCountries = DialectCatalog.getCountriesForLanguage("English")
        assertTrue(englishCountries.contains("United Kingdom"))
        assertTrue(englishCountries.contains("Ireland"))
        assertTrue(englishCountries.contains("Australia"))
        assertTrue(englishCountries.contains("United States"))
    }

    @Test
    fun testCountryToStateDrillDown() {
        // Level 3: States for Malayalam in India
        val keralaStates = DialectCatalog.getStatesForLanguageAndCountry("Malayalam", "India")
        assertEquals(listOf("Kerala"), keralaStates)

        // Level 3: States for English in UK
        val ukStates = DialectCatalog.getStatesForLanguageAndCountry("English", "United Kingdom")
        assertTrue(ukStates.contains("England"))
        assertTrue(ukStates.contains("Scotland"))

        // Level 3: States for English in US
        val usStates = DialectCatalog.getStatesForLanguageAndCountry("English", "United States")
        assertTrue(usStates.contains("New York"))
    }

    @Test
    fun testStateToDistrictDialectDrillDown() {
        // Level 4: Kerala districts
        val keralaDialects = DialectCatalog.getDialectsForHierarchy("Malayalam", "India", "Kerala")
        assertTrue("Kerala should contain at least 14 districts", keralaDialects.size >= 14)

        val districtNames = keralaDialects.map { it.cityOrArea }
        assertTrue(districtNames.contains("Kozhikode"))
        assertTrue(districtNames.contains("Thrissur"))
        assertTrue(districtNames.contains("Malappuram"))
        assertTrue(districtNames.contains("Ernakulam / Kochi"))
        assertTrue(districtNames.contains("Thiruvananthapuram"))
        assertTrue(districtNames.contains("Kannur"))
        assertTrue(districtNames.contains("Kasaragod"))

        // Level 4: England dialects
        val englandDialects = DialectCatalog.getDialectsForHierarchy("English", "United Kingdom", "England")
        val englandCities = englandDialects.map { it.cityOrArea }
        assertTrue(englandCities.contains("Liverpool"))
        assertTrue(englandCities.contains("London"))

        // Level 4: New York dialects
        val nyDialects = DialectCatalog.getDialectsForHierarchy("English", "United States", "New York")
        assertEquals(1, nyDialects.size)
        assertEquals("Brooklyn", nyDialects.first().cityOrArea)
    }

    @Test
    fun testSearchAcrossHierarchy() {
        // Search by district
        val kozhikodeSearch = DialectCatalog.searchDialects("Kozhikode")
        assertFalse(kozhikodeSearch.isEmpty())
        assertEquals("Kozhikode", kozhikodeSearch.first().cityOrArea)

        // Search by state
        val keralaSearch = DialectCatalog.searchDialects("Kerala")
        assertTrue(keralaSearch.size >= 14)

        // Search by country
        val ukSearch = DialectCatalog.searchDialects("United Kingdom")
        assertTrue(ukSearch.size >= 3)
    }

    @Test
    fun testFlagAndEmojiResolution() {
        assertEquals("🇮🇳", DialectCatalog.getFlagForCountry("India"))
        assertEquals("🇬🇧", DialectCatalog.getFlagForCountry("United Kingdom"))
        assertEquals("🇺🇸", DialectCatalog.getFlagForCountry("United States"))
        assertEquals("🌴", DialectCatalog.getLanguageEmoji("Malayalam"))
        assertEquals("🗣️", DialectCatalog.getLanguageEmoji("English"))
    }
}
