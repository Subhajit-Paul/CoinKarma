package com.coinkarma.app.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // singleton row
    val displayName: String = "Arjun",
    val avatarPath: String? = null,
    val karma: Int = 0,
    val streakDays: Int = 0,
    val dailyBudget: Int = 500,
    val savedModeOn: Boolean = false,
    val paletteDark: String  = "forest",
    val paletteLight: String = "forest",
    val darkMode: Boolean = true,
    val auraSkn: String = "default",
    val onboardingCompleted: Boolean = false,
)
