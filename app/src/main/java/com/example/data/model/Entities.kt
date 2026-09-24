package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_profiles")
data class VoiceProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileName: String,
    val dialectId: String,
    val language: String,
    val country: String,
    val region: String,
    val cityOrArea: String,
    val dialectName: String,
    val regionalStrength: Float = 0.75f,
    val personality: String = VoicePersonality.FRIENDLY.name,
    val responseStyle: String = "Casual",
    val slangEnabled: Boolean = true,
    val isDefault: Boolean = false
)

@Entity(tableName = "conversation_messages")
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 0,
    val role: String, // "user", "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dialectId: String,
    val highlightedSlangCsv: String = ""
)

@Entity(tableName = "dictionary_entries")
data class DictionaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val meaning: String,
    val region: String,
    val exampleSentence: String,
    val formalEquivalent: String,
    val category: String = "Slang",
    val isUserContributed: Boolean = false,
    val status: String = "Verified", // "Verified", "Pending Review"
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "speaking_style_preference")
data class SpeakingStylePreferenceEntity(
    @PrimaryKey
    val id: Long = 1,
    val learningEnabled: Boolean = false,
    val frequentlyUsedExpressionsCsv: String = "",
    val preferredSlangCsv: String = "",
    val formalityLevel: Float = 0.3f, // 0.0 casual -> 1.0 formal
    val sentenceStyle: String = "Conversational & rhythmic",
    val preferredResponseLength: String = "Medium",
    val preferredTone: String = "Warm & friendly",
    val frequentlyUsedWordsCsv: String = "",
    val codeSwitchingHabit: String = "Natural mixing with English"
)
