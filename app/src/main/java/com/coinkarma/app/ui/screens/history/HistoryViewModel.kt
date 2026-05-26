package com.coinkarma.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.profile.UserProfile
import com.coinkarma.app.data.transactions.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CatBreakdown(val cat: String, val amt: Int, val pct: Int)

data class HistoryUiState(
    val grouped: List<Pair<String, List<TransactionEntity>>> = emptyList(),
    val totalSpent: Double = 0.0,              // rolling 30-day sum
    val catBreakdown: List<CatBreakdown> = emptyList(),
    val spend30d: List<Double> = List(30) { 0.0 },
    val dailyBudget: Int = 500,
    val avgDaily: Int = 0,                     // avg over days that had any spend
    val zeroDays: Int = 0,                     // days with 0 spend in the 30d window
    val overDays: Int = 0,                     // days exceeding budget in the 30d window
    val momChangePct: Int? = null,             // null when previous 30d has no data
    val dayOfWeekTotals: List<Pair<String, Int>> = emptyList(),
    val spikeDayName: String? = null,          // null when no meaningful spike
    val spikeMultiple: Double? = null,
    val timeOfDay: List<Triple<String, String, Int>> = emptyList(),
    val longestStreak: Int = 0,                // longest under-budget run in 30d
    val mostSavedDay: Int = 0,                 // max (budget - spend) for an under-budget day
    val zeroSpendDaysAllTime: Int = 0,
)

class HistoryViewModel(private val db: CoinKarmaDatabase) : ViewModel() {

    private val dayFmt = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = combine(
        db.transactions().observeAll(),
        db.profile().observe().map { it ?: UserProfile() },
    ) { txs, profile ->
        val budget = profile.dailyBudget

        val spend30d = buildSpend30d(txs)
        val total30d = spend30d.sum()
        val activeDays = spend30d.count { it > 0 }
        val zeroDays = spend30d.count { it == 0.0 }
        val overDays = spend30d.count { it > budget }
        val avgDaily = if (activeDays > 0) (total30d / activeDays).toInt() else 0
        val momPct = buildMoM(txs, spend30d)

        // All-time grouping for Timeline tab
        val grouped = txs
            .groupBy { dayFmt.format(Date(it.timestamp)) }
            .entries.map { (day, list) -> day to list }

        // Category breakdown scoped to the 30d window
        val midnight = todayMidnight()
        val txs30d = txs.filter { it.timestamp >= midnight - 29 * DAY_MS }
        val catTotals = txs30d.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList().sortedByDescending { it.second }
        val catSum = catTotals.sumOf { it.second }.coerceAtLeast(1.0)
        val catBreakdown = catTotals.map { (cat, amt) ->
            CatBreakdown(cat, amt.toInt(), (amt / catSum * 100).toInt())
        }

        val dowTotals = buildDayOfWeekTotals(txs)
        val spike = findSpike(dowTotals)
        val timeOfDay = buildTimeOfDay(txs)
        val longestStreak = longestUnderBudgetStreak(spend30d, budget)
        val mostSavedDay = spend30d.maxOfOrNull { s ->
            if (s in 1.0..budget.toDouble()) budget - s.toInt() else 0
        } ?: 0

        HistoryUiState(
            grouped             = grouped,
            totalSpent          = total30d,
            catBreakdown        = catBreakdown,
            spend30d            = spend30d,
            dailyBudget         = budget,
            avgDaily            = avgDaily,
            zeroDays            = zeroDays,
            overDays            = overDays,
            momChangePct        = momPct,
            dayOfWeekTotals     = dowTotals,
            spikeDayName        = spike?.first,
            spikeMultiple       = spike?.second,
            timeOfDay           = timeOfDay,
            longestStreak       = longestStreak,
            mostSavedDay        = mostSavedDay,
            zeroSpendDaysAllTime = zeroDays,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun delete(tx: TransactionEntity) {
        viewModelScope.launch { db.transactions().delete(tx) }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun todayMidnight(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun buildSpend30d(txs: List<TransactionEntity>): List<Double> {
        val midnight = todayMidnight()
        return (29 downTo 0).map { daysAgo ->
            val start = midnight - daysAgo * DAY_MS
            txs.filter { it.timestamp in start until start + DAY_MS }.sumOf { it.amount }
        }
    }

    private fun buildMoM(txs: List<TransactionEntity>, spend30d: List<Double>): Int? {
        val midnight = todayMidnight()
        val prevStart = midnight - 59 * DAY_MS
        val prevEnd   = midnight - 29 * DAY_MS
        val prevTotal = txs.filter { it.timestamp in prevStart until prevEnd }.sumOf { it.amount }
        if (prevTotal == 0.0) return null
        return ((spend30d.sum() - prevTotal) / prevTotal * 100).toInt()
    }

    private fun buildDayOfWeekTotals(txs: List<TransactionEntity>): List<Pair<String, Int>> {
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")
        val totals = IntArray(7)
        txs.forEach { tx ->
            val dow = (Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                .get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7   // 0=Mon … 6=Sun
            totals[dow] += tx.amount.toInt()
        }
        return labels.mapIndexed { i, l -> l to totals[i] }
    }

    private fun findSpike(dowTotals: List<Pair<String, Int>>): Pair<String, Double>? {
        if (dowTotals.all { it.second == 0 }) return null
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val maxIdx = dowTotals.indices.maxByOrNull { dowTotals[it].second } ?: return null
        val maxVal = dowTotals[maxIdx].second.toDouble()
        if (maxVal == 0.0) return null
        val avgOthers = dowTotals.filterIndexed { i, _ -> i != maxIdx }
            .map { it.second.toDouble() }.average()
        val multiple = if (avgOthers > 0) maxVal / avgOthers else 2.0
        return if (multiple >= 1.3) dayNames[maxIdx] to multiple else null
    }

    private fun buildTimeOfDay(txs: List<TransactionEntity>): List<Triple<String, String, Int>> {
        val total = txs.sumOf { it.amount }.coerceAtLeast(1.0)
        val buckets = DoubleArray(4)
        txs.forEach { tx ->
            val h = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                .get(Calendar.HOUR_OF_DAY)
            buckets[when (h) { in 6..11 -> 0; in 12..16 -> 1; in 17..20 -> 2; else -> 3 }] += tx.amount
        }
        return listOf("Morning" to "6a–12p", "Afternoon" to "12–5p", "Evening" to "5–9p", "Late" to "9p–6a")
            .mapIndexed { i, (lbl, rng) -> Triple(lbl, rng, (buckets[i] / total * 100).toInt()) }
    }

    private fun longestUnderBudgetStreak(spend30d: List<Double>, budget: Int): Int {
        var max = 0; var cur = 0
        spend30d.forEach { s ->
            if (s > 0.0 && s <= budget) { if (++cur > max) max = cur } else cur = 0
        }
        return max
    }

    class Factory(private val db: CoinKarmaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(db) as T
    }

    private companion object {
        const val DAY_MS = 86_400_000L
    }
}
