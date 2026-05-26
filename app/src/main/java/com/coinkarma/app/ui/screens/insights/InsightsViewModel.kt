package com.coinkarma.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class DayTotal(val label: String, val amount: Double)

data class InsightsUiState(
    val categoryTotals: Map<String, Double> = emptyMap(),
    val weeklyTotals: List<DayTotal> = emptyList(),
    val topCategory: String? = null,
    val avgDaily: Double = 0.0,
)

class InsightsViewModel(private val db: CoinKarmaDatabase) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = db.transactions().observeAll()
        .map { txs ->
            val (weekStart, _) = weekRange()
            val weekTxs = txs.filter { it.timestamp >= weekStart }

            val categoryTotals = weekTxs
                .groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }
                .toMap()

            val weeklyTotals = buildWeeklyTotals(weekTxs)
            val topCategory = categoryTotals.entries.firstOrNull()?.key

            val days = if (weeklyTotals.isNotEmpty()) weeklyTotals.size.toDouble() else 1.0
            val avgDaily = weekTxs.sumOf { it.amount } / days

            InsightsUiState(
                categoryTotals = categoryTotals,
                weeklyTotals = weeklyTotals,
                topCategory = topCategory,
                avgDaily = avgDaily,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    private fun weekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, 7)
        return start to cal.timeInMillis
    }

    private fun buildWeeklyTotals(txs: List<com.coinkarma.app.data.transactions.TransactionEntity>): List<DayTotal> {
        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return (0..6).map { offset ->
            val dayCal = weekStart.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, offset)
            val label = dayLabels[dayCal.get(Calendar.DAY_OF_WEEK) - 1]
            val dayStart = dayCal.timeInMillis
            dayCal.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = dayCal.timeInMillis
            val total = txs.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.amount }
            DayTotal(label, total)
        }
    }

    class Factory(private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InsightsViewModel(db) as T
    }
}
