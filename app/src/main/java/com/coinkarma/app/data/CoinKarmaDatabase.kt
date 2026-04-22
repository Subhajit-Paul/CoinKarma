package com.coinkarma.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, CustomQuestEntity::class, UserProfile::class],
    version = 1,
    exportSchema = true
)
abstract class CoinKarmaDatabase : RoomDatabase() {
    abstract fun transactions(): TransactionDao
    abstract fun quests(): CustomQuestDao
    abstract fun profile(): UserProfileDao

    companion object {
        @Volatile private var INSTANCE: CoinKarmaDatabase? = null

        fun get(ctx: Context): CoinKarmaDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext,
                CoinKarmaDatabase::class.java,
                "coinkarma.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
