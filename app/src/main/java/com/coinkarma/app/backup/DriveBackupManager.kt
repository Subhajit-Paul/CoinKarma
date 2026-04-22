@file:Suppress("DEPRECATION")

package com.coinkarma.app.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.coinkarma.app.data.CoinKarmaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Drive backup — writes a JSON export of the Room DB to the user's appDataFolder
 * (private to CoinKarma, invisible in the normal Drive UI, restored when the
 * user signs in on a new device).
 *
 * Flow:
 *   1. Profile screen → "Connect Google" button → [signInIntent]
 *   2. Activity result → [onSignInResult] builds a Drive client
 *   3. Trigger [backupNow] / [restoreNow] manually or via WorkManager
 */
class DriveBackupManager(private val appContext: Context) {

    /** Request DRIVE_APPFOLDER scope — keep the blast radius small. */
    fun signInIntent(): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(appContext, gso).signInIntent
    }

    fun currentAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(appContext)

    private fun driveClient(account: GoogleSignInAccount): Drive {
        val cred = GoogleAccountCredential.usingOAuth2(
            appContext,
            listOf(DriveScopes.DRIVE_APPDATA),
        )
        cred.selectedAccount = account.account
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), cred)
            .setApplicationName("CoinKarma")
            .build()
    }

    suspend fun backupNow(db: CoinKarmaDatabase): Result<String> = withContext(Dispatchers.IO) {
        val account = currentAccount() ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        val drive = driveClient(account)
        val payload = exportJson(db)

        val metadata = File()
            .setName("coinkarma-backup-${System.currentTimeMillis()}.json")
            .setParents(listOf("appDataFolder"))
            .setMimeType("application/json")

        val content = com.google.api.client.http.ByteArrayContent("application/json", payload.toByteArray())
        val file = drive.files().create(metadata, content).setFields("id").execute()
        Result.success(file.id)
    }

    suspend fun restoreNow(db: CoinKarmaDatabase): Result<Int> = withContext(Dispatchers.IO) {
        val account = currentAccount() ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        val drive = driveClient(account)

        val list = drive.files().list()
            .setSpaces("appDataFolder")
            .setOrderBy("createdTime desc")
            .setPageSize(1)
            .setFields("files(id, name)")
            .execute()
        val latest = list.files.firstOrNull() ?: return@withContext Result.failure(IllegalStateException("No backup"))

        val stream = drive.files().get(latest.id).executeMediaAsInputStream()
        val json = stream.bufferedReader().use { it.readText() }
        val count = importJson(db, json)
        Result.success(count)
    }

    /** Minimal serializer — expand as you add more tables. */
    private suspend fun exportJson(db: CoinKarmaDatabase): String {
        val txs = db.transactions().observeAll().first()
        val quests = db.quests().observeAll().first()
        val profile = db.profile().observe().first()

        val root = JSONObject().apply {
            put("version", 1)
            put("transactions", JSONArray(txs.map { t ->
                JSONObject().apply {
                    put("amount", t.amount); put("category", t.category); put("note", t.note)
                    put("merchant", t.merchant); put("timestamp", t.timestamp); put("source", t.source)
                }
            }))
            put("quests", JSONArray(quests.map { q ->
                JSONObject().apply {
                    put("title", q.title); put("icon", q.icon); put("cap", q.cap)
                    put("days", q.days); put("xp", q.xp); put("rarity", q.rarity)
                    put("startedAt", q.startedAt); put("progressDays", q.progressDays)
                    put("completed", q.completed)
                }
            }))
            profile?.let {
                put("profile", JSONObject().apply {
                    put("displayName", it.displayName); put("avatarPath", it.avatarPath)
                    put("karma", it.karma); put("streakDays", it.streakDays)
                    put("dailyBudget", it.dailyBudget); put("darkMode", it.darkMode)
                })
            }
        }
        return root.toString()
    }

    private suspend fun importJson(db: CoinKarmaDatabase, json: String): Int {
        val root = JSONObject(json)
        var count = 0

        val txArray = root.optJSONArray("transactions")
        if (txArray != null) {
            repeat(txArray.length()) { i ->
                val o = txArray.getJSONObject(i)
                db.transactions().insert(
                    com.coinkarma.app.data.TransactionEntity(
                        amount    = o.getDouble("amount"),
                        category  = o.getString("category"),
                        note      = o.optString("note").ifEmpty { null },
                        merchant  = o.optString("merchant").ifEmpty { null },
                        timestamp = o.getLong("timestamp"),
                        source    = o.optString("source", "backup"),
                    )
                )
                count++
            }
        }

        val questArray = root.optJSONArray("quests")
        if (questArray != null) {
            repeat(questArray.length()) { i ->
                val o = questArray.getJSONObject(i)
                db.quests().insert(
                    com.coinkarma.app.data.CustomQuestEntity(
                        title       = o.getString("title"),
                        icon        = o.getString("icon"),
                        cap         = o.getInt("cap"),
                        days        = o.getInt("days"),
                        xp          = o.getInt("xp"),
                        rarity      = o.getString("rarity"),
                        startedAt   = o.getLong("startedAt"),
                        progressDays = o.optInt("progressDays", 0),
                        completed   = o.optBoolean("completed", false),
                    )
                )
                count++
            }
        }

        root.optJSONObject("profile")?.let { p ->
            db.profile().upsert(
                com.coinkarma.app.data.UserProfile(
                    displayName = p.optString("displayName", "Arjun"),
                    karma       = p.optInt("karma", 0),
                    streakDays  = p.optInt("streakDays", 0),
                    dailyBudget = p.optInt("dailyBudget", 500),
                    darkMode    = p.optBoolean("darkMode", true),
                )
            )
            count++
        }

        return count
    }
}
