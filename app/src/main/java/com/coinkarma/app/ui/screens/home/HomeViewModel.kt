package com.coinkarma.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.transactions.TransactionEntity
import com.coinkarma.app.data.profile.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val profile: UserProfile = UserProfile(),
    val todaySpent: Double = 0.0,
    val recentTxs: List<TransactionEntity> = emptyList(),
    val categoryBreakdown: Map<String, Double> = emptyMap(),
)

class HomeViewModel(private val db: CoinKarmaDatabase) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        db.profile().observe().map { it ?: UserProfile() },
        db.transactions().observeAll(),
    ) { profile, allTxs ->
        val (start, end) = todayRange()
        val todayTxs = allTxs.filter { it.timestamp in start until end }
        val todaySpent = todayTxs.sumOf { it.amount }
        val breakdown = todayTxs
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
        HomeUiState(
            profile = profile,
            todaySpent = todaySpent,
            recentTxs = allTxs.take(10),
            categoryBreakdown = breakdown,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return start to cal.timeInMillis
    }

    class Factory(private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(db) as T
    }
}
