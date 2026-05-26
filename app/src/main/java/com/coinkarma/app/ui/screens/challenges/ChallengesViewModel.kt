package com.coinkarma.app.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.quests.CustomQuestEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChallengesUiState(
    val active: List<CustomQuestEntity> = emptyList(),
    val completed: List<CustomQuestEntity> = emptyList(),
)

class ChallengesViewModel(private val db: CoinKarmaDatabase) : ViewModel() {

    val uiState: StateFlow<ChallengesUiState> = db.quests().observeAll()
        .map { quests ->
            ChallengesUiState(
                active    = quests.filter { !it.completed },
                completed = quests.filter { it.completed },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChallengesUiState())

    fun addDefaultQuests() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val defaults = listOf(
                CustomQuestEntity(title = "Zero-spend day",    icon = "🏆", cap = 0,   days = 1,  xp = 50,  rarity = "common",    startedAt = now),
                CustomQuestEntity(title = "₹200 food budget",  icon = "🍱", cap = 200, days = 3,  xp = 80,  rarity = "rare",      startedAt = now),
                CustomQuestEntity(title = "No impulse week",   icon = "🧘", cap = 100, days = 7,  xp = 150, rarity = "epic",      startedAt = now),
                CustomQuestEntity(title = "Savings streak",    icon = "💰", cap = 500, days = 30, xp = 500, rarity = "legendary", startedAt = now),
            )
            defaults.forEach { db.quests().insert(it) }
        }
    }

    fun createCustomQuest(title: String, icon: String, cap: Int, days: Int, xp: Int, rarity: String) {
        viewModelScope.launch {
            db.quests().insert(CustomQuestEntity(
                title = title, icon = icon, cap = cap, days = days, xp = xp,
                rarity = rarity, startedAt = System.currentTimeMillis(),
            ))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { db.quests().delete(id) }
    }

    class Factory(private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChallengesViewModel(db) as T
    }
}
