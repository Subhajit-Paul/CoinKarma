package com.coinkarma.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.profile.UserProfile
import com.coinkarma.app.platform.service.LocalBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val backupStatus: String? = null,
    val isBusy: Boolean = false,
)

class ProfileViewModel(
    private val db: CoinKarmaDatabase,
    private val backupManager: LocalBackupManager,
) : ViewModel() {

    private val _backupStatus = MutableStateFlow<String?>(null)
    private val _isBusy = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        db.profile().observe(),
        _backupStatus,
        _isBusy,
    ) { profile, status, busy ->
        ProfileUiState(
            profile = profile ?: UserProfile(),
            backupStatus = status,
            isBusy = busy,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            _backupStatus.value = "Exporting backup..."
            backupManager.exportToUri(db, uri).fold(
                onSuccess = { _backupStatus.value = "Exported $it items" },
                onFailure = { _backupStatus.value = "Export failed: ${it.message}" },
            )
            _isBusy.value = false
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            _backupStatus.value = "Importing backup..."
            backupManager.importFromUri(db, uri).fold(
                onSuccess = { _backupStatus.value = "Imported $it items" },
                onFailure = { _backupStatus.value = "Import failed: ${it.message}" },
            )
            _isBusy.value = false
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            clearLocalData()
            _backupStatus.value = "All data deleted"
        }
    }

    private suspend fun clearLocalData(onboardingCompleted: Boolean = true) {
        db.transactions().deleteAll()
        db.quests().deleteAll()
        db.profile().deleteAll()
        db.profile().upsert(UserProfile(onboardingCompleted = onboardingCompleted))
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            db.profile().upsert(uiState.value.profile.copy(darkMode = enabled))
        }
    }

    fun setDailyBudget(budget: Int) {
        viewModelScope.launch {
            db.profile().upsert(uiState.value.profile.copy(dailyBudget = budget))
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            db.profile().upsert(uiState.value.profile.copy(displayName = name))
        }
    }

    fun setAvatar(path: String) {
        viewModelScope.launch {
            db.profile().upsert(uiState.value.profile.copy(avatarPath = path))
        }
    }

    fun setPalette(dark: String? = null, light: String? = null) {
        viewModelScope.launch {
            val current = uiState.value.profile
            db.profile().upsert(current.copy(
                paletteDark  = dark  ?: current.paletteDark,
                paletteLight = light ?: current.paletteLight,
            ))
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            db.profile().upsert(uiState.value.profile.copy(onboardingCompleted = true))
        }
    }

    class Factory(private val db: CoinKarmaDatabase, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(db, LocalBackupManager(context.applicationContext)) as T
    }
}
