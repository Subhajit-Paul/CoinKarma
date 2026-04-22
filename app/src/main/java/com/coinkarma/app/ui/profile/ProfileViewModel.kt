package com.coinkarma.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val backupStatus: String? = null,
    val isBacking: Boolean = false,
)

class ProfileViewModel(private val db: CoinKarmaDatabase) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = db.profile().observe()
        .map { ProfileUiState(profile = it ?: UserProfile()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            db.profile().upsert(uiState.value.profile.copy(darkMode = enabled))
        }
    }

    fun setDailyBudget(budget: Int) {
        viewModelScope.launch {
            val current = uiState.value.profile
            db.profile().upsert(current.copy(dailyBudget = budget))
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            val current = uiState.value.profile
            db.profile().upsert(current.copy(displayName = name))
        }
    }

    class Factory(private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(db) as T
    }
}
