package com.coinkarma.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coinkarma.app.data.profile.UserProfile
import com.coinkarma.app.data.profile.UserProfileDao
import com.coinkarma.app.data.quests.CustomQuestDao
import com.coinkarma.app.data.quests.CustomQuestEntity
import com.coinkarma.app.data.transactions.TransactionDao
import com.coinkarma.app.data.transactions.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CustomQuestEntity::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
abstract class CoinKarmaDatabase : RoomDatabase() {
    abstract fun transactions(): TransactionDao
    abstract fun quests(): CustomQuestDao
    abstract fun profile(): UserProfileDao

    companion object {
        @Volatile
        private var instance: CoinKarmaDatabase? = null

        fun get(context: Context): CoinKarmaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CoinKarmaDatabase::class.java,
                    "coinkarma_db"
                ).fallbackToDestructiveMigration(true).build().also { instance = it }
            }
        }
    }
}
