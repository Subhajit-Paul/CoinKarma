package com.coinkarma.app.data.quests

import androidx.room.Entity
import androidx.room.PrimaryKey

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
