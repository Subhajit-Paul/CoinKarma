package com.coinkarma.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val grouped: List<Pair<String, List<TransactionEntity>>> = emptyList(),
    val totalSpent: Double = 0.0,
)

class HistoryViewModel(private val db: CoinKarmaDatabase) : ViewModel() {

    private val dayFmt = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = db.transactions().observeAll()
        .map { txs ->
            val grouped = txs
                .groupBy { dayFmt.format(Date(it.timestamp)) }
                .entries
                .map { (day, list) -> day to list }
            HistoryUiState(
                grouped = grouped,
                totalSpent = txs.sumOf { it.amount },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun delete(tx: TransactionEntity) {
        viewModelScope.launch { db.transactions().delete(tx) }
    }

    class Factory(private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(db) as T
    }
}
