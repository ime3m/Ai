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

    // --- Conversation Sessions ---
    @Query("SELECT * FROM conversation_sessions WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveSessions(): Flow<List<com.example.data.model.ConversationSessionEntity>>

    @Query("SELECT * FROM conversation_sessions WHERE isPinned = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getPinnedSessions(): Flow<List<com.example.data.model.ConversationSessionEntity>>

    @Query("SELECT * FROM conversation_sessions WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedSessions(): Flow<List<com.example.data.model.ConversationSessionEntity>>

    @Query("SELECT * FROM conversation_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): com.example.data.model.ConversationSessionEntity?

    @Query("SELECT * FROM conversation_sessions WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR lastMessagePreview LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchSessions(query: String): Flow<List<com.example.data.model.ConversationSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: com.example.data.model.ConversationSessionEntity)

    @Update
    suspend fun updateSession(session: com.example.data.model.ConversationSessionEntity)

    @Query("UPDATE conversation_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun setSessionPinned(sessionId: String, isPinned: Boolean)

    @Query("UPDATE conversation_sessions SET isArchived = :isArchived WHERE id = :sessionId")
    suspend fun setSessionArchived(sessionId: String, isArchived: Boolean)

    @Query("UPDATE conversation_sessions SET title = :title WHERE id = :sessionId")
    suspend fun renameSession(sessionId: String, title: String)

    @Query("DELETE FROM conversation_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    // --- Conversation History ---
    @Query("SELECT * FROM conversation_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM (SELECT * FROM conversation_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getRecentMessagesForConversation(conversationId: String, limit: Int = 30): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages WHERE conversationId = :conversationId AND timestamp < :beforeTimestamp ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getOlderMessages(conversationId: String, beforeTimestamp: Long, limit: Int = 30): List<ConversationMessageEntity>

    @Query("SELECT * FROM conversation_messages WHERE dialectId = :dialectId ORDER BY timestamp ASC")
    fun getMessagesForDialect(dialectId: String): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp DESC")
    fun getAllMessagesDesc(): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchAllMessages(query: String): Flow<List<ConversationMessageEntity>>

    @Query("SELECT COUNT(*) FROM conversation_messages")
    fun getMessageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessageEntity): Long

    @Query("DELETE FROM conversation_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("UPDATE conversation_messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun setMessagePinned(messageId: Long, isPinned: Boolean)

    @Query("DELETE FROM conversation_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

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
