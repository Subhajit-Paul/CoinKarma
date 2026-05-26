package com.coinkarma.app.platform.service

import android.content.Context
import android.net.Uri
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.profile.UserProfile
import com.coinkarma.app.data.quests.CustomQuestEntity
import com.coinkarma.app.data.transactions.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LocalBackupManager(private val appContext: Context) {

    suspend fun exportToUri(db: CoinKarmaDatabase, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = exportJson(db)
            appContext.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(payload.toByteArray(Charsets.UTF_8))
            } ?: error("Could not open export file")
            exportedItemCount(db)
        }
    }

    suspend fun importFromUri(db: CoinKarmaDatabase, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val json = appContext.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Could not open import file")
            importJson(db, json)
        }
    }

    private suspend fun exportedItemCount(db: CoinKarmaDatabase): Int {
        val txs = db.transactions().observeAll().first().size
        val quests = db.quests().observeAll().first().size
        val profile = if (db.profile().observe().first() != null) 1 else 0
        return txs + quests + profile
    }

    private suspend fun exportJson(db: CoinKarmaDatabase): String {
        val txs = db.transactions().observeAll().first()
        val quests = db.quests().observeAll().first()
        val profile = db.profile().observe().first()

        val root = JSONObject().apply {
            put("app", "CoinKarma")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("transactions", JSONArray(txs.map { t ->
                JSONObject().apply {
                    put("amount", t.amount)
                    put("category", t.category)
                    put("note", t.note)
                    put("merchant", t.merchant)
                    put("timestamp", t.timestamp)
                    put("source", t.source)
                }
            }))
            put("quests", JSONArray(quests.map { q ->
                JSONObject().apply {
                    put("title", q.title)
                    put("icon", q.icon)
                    put("cap", q.cap)
                    put("days", q.days)
                    put("xp", q.xp)
                    put("rarity", q.rarity)
                    put("startedAt", q.startedAt)
                    put("progressDays", q.progressDays)
                    put("completed", q.completed)
                }
            }))
            profile?.let {
                put("profile", JSONObject().apply {
                    put("displayName", it.displayName)
                    put("avatarPath", it.avatarPath)
                    put("karma", it.karma)
                    put("streakDays", it.streakDays)
                    put("dailyBudget", it.dailyBudget)
                    put("savedModeOn", it.savedModeOn)
                    put("paletteDark", it.paletteDark)
                    put("paletteLight", it.paletteLight)
                    put("darkMode", it.darkMode)
                    put("auraSkn", it.auraSkn)
                    put("onboardingCompleted", it.onboardingCompleted)
                })
            }
        }
        return root.toString()
    }

    private suspend fun importJson(db: CoinKarmaDatabase, json: String): Int {
        val root = JSONObject(json)
        require(root.optString("app", "CoinKarma") == "CoinKarma") { "Not a CoinKarma backup file" }

        db.transactions().deleteAll()
        db.quests().deleteAll()
        db.profile().deleteAll()

        var count = 0
        root.optJSONArray("transactions")?.let { txArray ->
            repeat(txArray.length()) { i ->
                val o = txArray.getJSONObject(i)
                db.transactions().insert(
                    TransactionEntity(
                        amount = o.getDouble("amount"),
                        category = o.getString("category"),
                        note = o.optString("note").ifEmpty { null },
                        merchant = o.optString("merchant").ifEmpty { null },
                        timestamp = o.getLong("timestamp"),
                        source = o.optString("source", "backup"),
                    )
                )
                count++
            }
        }

        root.optJSONArray("quests")?.let { questArray ->
            repeat(questArray.length()) { i ->
                val o = questArray.getJSONObject(i)
                db.quests().insert(
                    CustomQuestEntity(
                        title = o.getString("title"),
                        icon = o.getString("icon"),
                        cap = o.getInt("cap"),
                        days = o.getInt("days"),
                        xp = o.getInt("xp"),
                        rarity = o.getString("rarity"),
                        startedAt = o.getLong("startedAt"),
                        progressDays = o.optInt("progressDays", 0),
                        completed = o.optBoolean("completed", false),
                    )
                )
                count++
            }
        }

        root.optJSONObject("profile")?.let { p ->
            db.profile().upsert(
                UserProfile(
                    displayName = p.optString("displayName", "Arjun"),
                    avatarPath = p.optString("avatarPath").ifEmpty { null },
                    karma = p.optInt("karma", 0),
                    streakDays = p.optInt("streakDays", 0),
                    dailyBudget = p.optInt("dailyBudget", 500),
                    savedModeOn = p.optBoolean("savedModeOn", false),
                    paletteDark = p.optString("paletteDark", "forest"),
                    paletteLight = p.optString("paletteLight", "forest"),
                    darkMode = p.optBoolean("darkMode", true),
                    auraSkn = p.optString("auraSkn", "default"),
                    onboardingCompleted = p.optBoolean("onboardingCompleted", true),
                )
            )
            count++
        } ?: db.profile().upsert(UserProfile(onboardingCompleted = true))

        return count
    }
}
