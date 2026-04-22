package com.coinkarma.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Spend event. One row per logged transaction (manual or SMS-parsed).
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "category") val category: String,  // "food" | "transport" | ...
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "merchant") val merchant: String? = null,
    @ColumnInfo(name = "timestamp") val timestamp: Long,  // epoch millis
    @ColumnInfo(name = "source") val source: String = "manual", // "manual" | "sms"
    @ColumnInfo(name = "sms_raw") val smsRaw: String? = null,
)

@Entity(tableName = "custom_quests")
data class CustomQuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String,           // emoji
    val cap: Int,               // 0 = zero-spend
    val days: Int,
    val xp: Int,
    val rarity: String,         // "common" | "rare" | "epic" | "legendary"
    val startedAt: Long,
    val progressDays: Int = 0,
    val completed: Boolean = false,
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // singleton row
    val displayName: String = "Arjun",
    val avatarPath: String? = null,
    val karma: Int = 0,
    val streakDays: Int = 0,
    val dailyBudget: Int = 500,
    val savedModeOn: Boolean = false,
    val paletteDark: String = "forest",
    val paletteLight: String = "forest",
    val darkMode: Boolean = true,
)
