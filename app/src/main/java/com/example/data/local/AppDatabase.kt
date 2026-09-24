package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.DictionaryEntryEntity
import com.example.data.model.SpeakingStylePreferenceEntity
import com.example.data.model.VoiceProfileEntity

@Database(
    entities = [
        VoiceProfileEntity::class,
        ConversationMessageEntity::class,
        DictionaryEntryEntity::class,
        SpeakingStylePreferenceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voiceDao(): VoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "regional_voice_ai.db"
                ).fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
