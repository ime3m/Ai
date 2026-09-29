package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Regional Voice AI", appName)
  }

  @Test
  fun `verify futuristic tokens corner radii and colors`() {
    assertEquals(12.0f, com.example.ui.theme.FuturisticTokens.CornerRadius.small.value)
    assertEquals(20.0f, com.example.ui.theme.FuturisticTokens.CornerRadius.medium.value)
    assertEquals(28.0f, com.example.ui.theme.FuturisticTokens.CornerRadius.large.value)
    assertEquals(999.0f, com.example.ui.theme.FuturisticTokens.CornerRadius.pill.value)

    // Verify 8dp-based spacing
    assertEquals(8.0f, com.example.ui.theme.FuturisticTokens.Spacing.xs.value)
    assertEquals(16.0f, com.example.ui.theme.FuturisticTokens.Spacing.md.value)
    assertEquals(24.0f, com.example.ui.theme.FuturisticTokens.Spacing.lg.value)

    // Verify requested categories: Voice, Languages, Accents, Translate, Conversation
    val categories = com.example.ui.components.futuristic.FuturisticCategory.entries.map { it.title }
    assertEquals(5, categories.size)
    org.junit.Assert.assertTrue(categories.contains("Voice"))
    org.junit.Assert.assertTrue(categories.contains("Languages"))
    org.junit.Assert.assertTrue(categories.contains("Accents"))
    org.junit.Assert.assertTrue(categories.contains("Translate"))
    org.junit.Assert.assertTrue(categories.contains("Conversation"))
  }

  @Test
  fun `verify default regional profile is Kerala and registry profiles exist`() {
    val defaultProfile = com.example.data.regional.RegionalProfileRegistry.KERALA
    assertEquals("kerala", defaultProfile.id)
    assertEquals("Kerala", defaultProfile.name)
    assertEquals("ml", defaultProfile.defaultLanguage)
    assertEquals("ml-IN", defaultProfile.defaultLocaleCode)

    // Verify key regional profiles
    val all = com.example.data.regional.RegionalProfileRegistry.allProfiles
    val ids = all.map { it.id }
    org.junit.Assert.assertTrue(ids.contains("kerala"))
    org.junit.Assert.assertTrue(ids.contains("india"))
    org.junit.Assert.assertTrue(ids.contains("tamil_nadu"))
    org.junit.Assert.assertTrue(ids.contains("karnataka"))
    org.junit.Assert.assertTrue(ids.contains("philippines"))
    org.junit.Assert.assertTrue(ids.contains("kuwait"))
    org.junit.Assert.assertTrue(ids.contains("united_states"))
    org.junit.Assert.assertTrue(ids.contains("united_kingdom"))
    org.junit.Assert.assertTrue(ids.contains("japan"))
  }

  @Test
  fun `verify preferences persistence and initial defaults`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefsRepo = com.example.data.preferences.PreferencesRepository(context)

    // Initial default should be Kerala
    assertEquals("kerala", prefsRepo.selectedRegionId)
    assertEquals("ml", prefsRepo.selectedLanguage)

    // Test saving new region
    prefsRepo.selectedRegionId = "tamil_nadu"
    prefsRepo.selectedLanguage = "ta"
    assertEquals("tamil_nadu", prefsRepo.selectedRegionId)
    assertEquals("ta", prefsRepo.selectedLanguage)

    // Reset back to Kerala for test cleanliness
    prefsRepo.selectedRegionId = "kerala"
    prefsRepo.selectedLanguage = "ml"
  }

  @Test
  fun `verify first launch flag detects initial state and completes onboarding`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefsRepo = com.example.data.preferences.PreferencesRepository(context)

    // Reset flag for test
    prefsRepo.hasCompletedFirstLaunch = false
    org.junit.Assert.assertFalse(prefsRepo.hasCompletedFirstLaunch)
    assertEquals("kerala", prefsRepo.selectedRegionId)

    // Complete first launch
    prefsRepo.hasCompletedFirstLaunch = true
    org.junit.Assert.assertTrue(prefsRepo.hasCompletedFirstLaunch)
  }

  @Test
  fun `verify AIConfig creates general-purpose prompt without restrictive topic bounds`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val prompt = com.example.data.ai.AIConfig.buildSystemInstruction(
      regionalProfile = kerala,
      dialect = dialect,
      strength = 0.75f,
      personality = com.example.data.model.VoicePersonality.FRIENDLY,
      speakingStyle = null,
      slangEnabled = true,
      responseLength = "Balanced",
      naturalMixingEnabled = true
    )

    // Verify general-purpose knowledge mandate is present
    org.junit.Assert.assertTrue(prompt.contains("general-purpose AI assistant"))
    org.junit.Assert.assertTrue(prompt.contains("REGIONAL STYLE IS NOT A KNOWLEDGE LIMIT"))
    org.junit.Assert.assertTrue(prompt.contains("Kerala is your DEFAULT regional personality"))
    org.junit.Assert.assertTrue(prompt.contains("Programming"))
    org.junit.Assert.assertTrue(prompt.contains("ABSOLUTE PROHIBITION ON REPETITIVE FILLER PHRASES"))
  }

  // =========================================================================
  // MANDATORY BUG FIX TEST CASES: SEPARATING REGION STYLE FROM LOCATION
  // =========================================================================

  @Test
  fun `test case 1 - selected region Kerala with weather in Kuwait queries Kuwait`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "Weather in Kuwait"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(query)
    assertEquals("Kuwait", queryContext.requestedLocation)
    org.junit.Assert.assertTrue(queryContext.isLocationExplicit)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation
    )
    org.junit.Assert.assertNotNull(response)
    // Factual weather data must be Kuwait data, NOT Kerala data!
    org.junit.Assert.assertTrue(response!!.factualText.contains("Kuwait"))
    org.junit.Assert.assertFalse(response.factualText.contains("Kerala (Statewide)"))
    // Response dialect text is in Kerala Malayalam style
    org.junit.Assert.assertTrue(response.dialectText.contains("കുവൈറ്റിൽ") || response.dialectText.contains("Kuwait"))
  }

  @Test
  fun `test case 2 - selected region Kerala with weather in Dubai queries Dubai`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "Weather in Dubai"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(query)
    assertEquals("Dubai", queryContext.requestedLocation)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation
    )
    org.junit.Assert.assertNotNull(response)
    org.junit.Assert.assertTrue(response!!.factualText.contains("Dubai"))
    org.junit.Assert.assertFalse(response.factualText.contains("Kerala (Statewide)"))
    org.junit.Assert.assertTrue(response.dialectText.contains("ദുബായിൽ") || response.dialectText.contains("Dubai"))
  }

  @Test
  fun `test case 3 - selected region Kerala with weather in Kochi queries Kochi`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "Weather in Kochi"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(query)
    assertEquals("Kochi", queryContext.requestedLocation)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation
    )
    org.junit.Assert.assertNotNull(response)
    org.junit.Assert.assertTrue(response!!.factualText.contains("Kochi"))
    org.junit.Assert.assertTrue(response.dialectText.contains("കൊച്ചിയിൽ") || response.dialectText.contains("Kochi"))
  }

  @Test
  fun `test case 4 - selected region Kerala with unlocatable weather query asks for location or uses user configured location`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "What is the weather?"

    // Case 4A: Without user-configured location, asks user for clarification
    val queryContextUnspecified = com.example.data.location.LocationExtractor.analyzeQuery(
      query = query,
      userLocation = com.example.data.location.UserLocation()
    )
    org.junit.Assert.assertNull(queryContextUnspecified.requestedLocation)
    org.junit.Assert.assertTrue(queryContextUnspecified.requiresLocationClarification)

    val responseUnspecified = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = null,
      userLocation = com.example.data.location.UserLocation()
    )
    org.junit.Assert.assertNotNull(responseUnspecified)
    // Asks user for location in Malayalam regional style
    org.junit.Assert.assertTrue(responseUnspecified!!.dialectText.contains("ഏത് സ്ഥലത്തെ") || responseUnspecified.dialectText.contains("Which location"))

    // Case 4B: With user-configured location (e.g. Dubai), uses that location
    val userConfiguredLoc = com.example.data.location.UserLocation(
      city = "Dubai",
      country = "UAE",
      source = com.example.data.location.UserLocation.SOURCE_USER_CONFIGURED
    )
    val queryContextConfigured = com.example.data.location.LocationExtractor.analyzeQuery(
      query = query,
      userLocation = userConfiguredLoc
    )
    assertEquals("Dubai", queryContextConfigured.requestedLocation)
    org.junit.Assert.assertFalse(queryContextConfigured.requiresLocationClarification)
  }

  @Test
  fun `test case 5 - weather there resolves previous location from context`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val history = listOf(
      "user" to "What is happening in Kuwait?",
      "assistant" to "Kuwait is having clear skies."
    )
    val query = "What's the weather there?"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(
      query = query,
      conversationHistory = history
    )
    assertEquals("Kuwait", queryContext.requestedLocation)
    org.junit.Assert.assertTrue(queryContext.resolvedFromContext)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation,
      conversationHistory = history
    )
    org.junit.Assert.assertNotNull(response)
    org.junit.Assert.assertTrue(response!!.factualText.contains("Kuwait"))
  }

  @Test
  fun `test case 6 - time in Tokyo returns Tokyo time in Kerala regional style`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "What's the time in Tokyo?"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(query)
    assertEquals("Tokyo", queryContext.requestedLocation)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation
    )
    org.junit.Assert.assertNotNull(response)
    org.junit.Assert.assertTrue(response!!.factualText.contains("Tokyo"))
    org.junit.Assert.assertTrue(response.dialectText.contains("ടോക്കിയോ") || response.dialectText.contains("Tokyo"))
  }

  @Test
  fun `test case 7 - restaurants in Kuwait City returns Kuwait City dining in Kerala regional style`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "Find restaurants in Kuwait City"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(query)
    assertEquals("Kuwait City", queryContext.requestedLocation)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation
    )
    org.junit.Assert.assertNotNull(response)
    org.junit.Assert.assertTrue(response!!.factualText.contains("Kuwait City"))
    org.junit.Assert.assertTrue(response.dialectText.contains("കുവൈറ്റ് സിറ്റി") || response.dialectText.contains("Kuwait City"))
  }

  @Test
  fun `test case 8 - happening in London returns London information in Kerala regional style`() {
    val kerala = com.example.data.regional.RegionalProfileRegistry.KERALA
    val dialect = kerala.toRegionalDialect()
    val query = "What's happening in London?"

    val queryContext = com.example.data.location.LocationExtractor.analyzeQuery(query)
    assertEquals("London", queryContext.requestedLocation)

    val response = com.example.data.knowledge.RealTimeKnowledgeEngine.retrieveVerifiedKnowledge(
      query = query,
      dialect = dialect,
      resolvedLocation = queryContext.requestedLocation
    )
    org.junit.Assert.assertNotNull(response)
    org.junit.Assert.assertTrue(response!!.factualText.contains("London"))
    org.junit.Assert.assertFalse(response.factualText.contains("Vizhinjam"))
    org.junit.Assert.assertTrue(
      response.dialectText.contains("ലണ്ടൻ") ||
      response.dialectText.contains("ലണ്ടനിലെ") ||
      response.dialectText.contains("London")
    )
  }

  // =========================================================================
  // LOCATION RESOLVER TESTS
  // =========================================================================

  @Test
  fun `LocationResolver extracts explicit location entities and prioritizes detected location over regional profile`() {
    val resolver = com.example.data.location.LocationResolver
    val keralaProfile = com.example.data.regional.RegionalProfileRegistry.KERALA

    // 1. Kuwait query with Kerala regional profile
    val resultKuwait = resolver.resolveLocation(
      query = "What is the weather in Kuwait?",
      regionalProfile = keralaProfile
    )
    assertEquals("Kuwait", resultKuwait.location)
    assertEquals(com.example.data.location.LocationSource.EXPLICIT_QUERY, resultKuwait.source)
    org.junit.Assert.assertTrue(resultKuwait.isExplicit)
    org.junit.Assert.assertFalse(resultKuwait.requiresClarification)

    // 2. Dubai query with Kerala regional profile
    val resultDubai = resolver.resolveLocation(
      query = "Dubai weather tomorrow",
      regionalProfile = keralaProfile
    )
    assertEquals("Dubai", resultDubai.location)
    assertEquals(com.example.data.location.LocationSource.EXPLICIT_QUERY, resultDubai.source)

    // 3. Malayalam query with Kerala regional profile
    val resultErnakulam = resolver.resolveLocation(
      query = "എറണാകുളത്ത് ഇന്ന് മഴ പെയ്യുമോ?",
      regionalProfile = keralaProfile
    )
    assertEquals("Ernakulam", resultErnakulam.location)
    assertEquals(com.example.data.location.LocationSource.EXPLICIT_QUERY, resultErnakulam.source)

    // 4. "Weather there" contextual resolution
    val history = listOf(
      "user" to "How is the food in Tokyo?",
      "assistant" to "Tokyo has world-class ramen and sushi."
    )
    val resultThere = resolver.resolveLocation(
      query = "What's the weather there?",
      conversationHistory = history,
      regionalProfile = keralaProfile
    )
    assertEquals("Tokyo", resultThere.location)
    assertEquals(com.example.data.location.LocationSource.CONVERSATION_CONTEXT, resultThere.source)

    // 5. Unspecified weather query with Kerala profile must NOT default to Kerala
    val resultUnspecified = resolver.resolveLocation(
      query = "What is the weather?",
      regionalProfile = keralaProfile,
      userLocation = com.example.data.location.UserLocation()
    )
    org.junit.Assert.assertNull(resultUnspecified.location)
    org.junit.Assert.assertTrue(resultUnspecified.requiresClarification)
  }
}

