package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.DictionaryEntryEntity
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.VoiceProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceDao {

    // --- Voice Profiles ---
    @Query("SELECT * FROM voice_profiles ORDER BY isDefault DESC, id ASC")
    fun getAllProfiles(): Flow<List<VoiceProfileEntity>>

    @Query("SELECT * FROM voice_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): VoiceProfileEntity?

    @Query("SELECT * FROM voice_profiles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultProfile(): VoiceProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VoiceProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: VoiceProfileEntity)

    @Query("UPDATE voice_profiles SET isDefault = 0")
    suspend fun clearDefaultProfiles()

    @Query("UPDATE voice_profiles SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultProfile(id: Long)

    @Delete
    suspend fun deleteProfile(profile: VoiceProfileEntity)

    // --- Conversation History ---
    @Query("SELECT * FROM conversation_messages WHERE dialectId = :dialectId ORDER BY timestamp ASC")
    fun getMessagesForDialect(dialectId: String): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessageEntity): Long

    @Query("DELETE FROM conversation_messages WHERE dialectId = :dialectId")
    suspend fun deleteMessagesForDialect(dialectId: String)

    @Query("DELETE FROM conversation_messages")
    suspend fun clearAllMessages()

    // --- Dictionary Entries ---
    @Query("SELECT * FROM dictionary_entries ORDER BY createdTimestamp DESC")
    fun getAllDictionaryEntries(): Flow<List<DictionaryEntryEntity>>

    @Query("SELECT * FROM dictionary_entries WHERE region LIKE '%' || :filter || '%' OR expression LIKE '%' || :filter || '%' ORDER BY createdTimestamp DESC")
    fun searchDictionary(filter: String): Flow<List<DictionaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDictionaryEntry(entry: DictionaryEntryEntity): Long

    @Update
    suspend fun updateDictionaryEntry(entry: DictionaryEntryEntity)

    @Delete
    suspend fun deleteDictionaryEntry(entry: DictionaryEntryEntity)

    // --- Speaking Style Preferences (Learn My Speaking Style) ---
    @Query("SELECT * FROM speaking_style_preference WHERE id = 1 LIMIT 1")
    fun getSpeakingStyle(): Flow<SpeakingStylePreferenceEntity?>

    @Query("SELECT * FROM speaking_style_preference WHERE id = 1 LIMIT 1")
    suspend fun getSpeakingStyleSync(): SpeakingStylePreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSpeakingStyle(style: SpeakingStylePreferenceEntity)

    @Query("DELETE FROM speaking_style_preference")
    suspend fun clearSpeakingStyle()
}
