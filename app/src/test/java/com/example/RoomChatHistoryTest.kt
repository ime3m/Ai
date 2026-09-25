package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.VoiceDao
import com.example.data.model.ConversationMessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomChatHistoryTest {

    private lateinit var database: AppDatabase
    private lateinit var voiceDao: VoiceDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        voiceDao = database.voiceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test inserting and querying conversation messages`() = runBlocking {
        val convId = "test_conversation_123"

        val userMessage = ConversationMessageEntity(
            conversationId = convId,
            role = "user",
            text = "എങ്ങനെ ഉണ്ട് വിശേഷം?",
            dialectId = "ml_in_kl_kozhikode",
            timestamp = 1000L
        )
        val assistantMessage = ConversationMessageEntity(
            conversationId = convId,
            role = "assistant",
            text = "സുഖം തന്നെയല്ലേ, എന്തൊക്കെയുണ്ട് കാര്യം?",
            dialectId = "ml_in_kl_kozhikode",
            timestamp = 2000L
        )

        val id1 = voiceDao.insertMessage(userMessage)
        val id2 = voiceDao.insertMessage(assistantMessage)

        assertTrue(id1 > 0)
        assertTrue(id2 > 0)

        // Query messages for this conversation (ordered ascending)
        val messages = voiceDao.getMessagesForConversation(convId).first()
        assertEquals(2, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals("എങ്ങനെ ഉണ്ട് വിശേഷം?", messages[0].text)
        assertEquals("assistant", messages[1].role)
        assertEquals("സുഖം തന്നെയല്ലേ, എന്തൊക്കെയുണ്ട് കാര്യം?", messages[1].text)
    }

    @Test
    fun `test searching and deleting messages from Room database`() = runBlocking {
        val convId = "session_search_test"

        val msg1 = ConversationMessageEntity(
            conversationId = convId,
            role = "user",
            text = "Where is the best biryani in Kozhikode?",
            dialectId = "ml_in_kl_kozhikode"
        )
        val msg2 = ConversationMessageEntity(
            conversationId = convId,
            role = "assistant",
            text = "Paragon Restaurant serves authentic Kozhikode dum biryani!",
            dialectId = "ml_in_kl_kozhikode"
        )

        val id1 = voiceDao.insertMessage(msg1)
        val id2 = voiceDao.insertMessage(msg2)

        // Search messages containing "biryani"
        val searchResults = voiceDao.searchAllMessages("biryani").first()
        assertEquals(2, searchResults.size)

        // Delete first message
        voiceDao.deleteMessageById(id1)
        val afterDelete = voiceDao.getMessagesForConversation(convId).first()
        assertEquals(1, afterDelete.size)
        assertEquals(id2, afterDelete[0].id)

        // Clear all
        voiceDao.clearAllMessages()
        val allAfterClear = voiceDao.getAllMessages().first()
        assertTrue(allAfterClear.isEmpty())
    }
}
