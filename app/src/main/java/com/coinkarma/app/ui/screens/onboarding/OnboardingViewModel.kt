package com.coinkarma.app.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.profile.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 0,
    val profile: UserProfile = UserProfile(),
)

class OnboardingViewModel(
    app: Application,
    private val db: CoinKarmaDatabase
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            db.profile().observe().collect { profile ->
                if (profile != null) {
                    _uiState.update { it.copy(profile = profile) }
                }
            }
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(step = it.step + 1) }
    }

    fun prevStep() {
        if (_uiState.value.step > 0) {
            _uiState.update { it.copy(step = it.step - 1) }
        }
    }

    fun updateBudget(budget: Int) {
        viewModelScope.launch {
            val current = _uiState.value.profile
            db.profile().upsert(current.copy(dailyBudget = budget))
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            val current = _uiState.value.profile
            db.profile().upsert(current.copy(displayName = name))
        }
    }

    fun updateAvatar(path: String) {
        viewModelScope.launch {
            val current = _uiState.value.profile
            db.profile().upsert(current.copy(avatarPath = path))
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val current = _uiState.value.profile
            db.profile().upsert(current.copy(onboardingCompleted = true))
        }
    }

    class Factory(private val app: Application, private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OnboardingViewModel(app, db) as T
    }
}
