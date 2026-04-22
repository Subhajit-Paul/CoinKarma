package com.coinkarma.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.coinkarma.app.backup.BackupWorker
import com.coinkarma.app.data.CoinKarmaDatabase

class CoinKarmaApp : Application() {
    val db by lazy { CoinKarmaDatabase.get(this) }

    override fun onCreate() {
        super.onCreate()
        createChannels()
        BackupWorker.schedule(this)
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                CHANNEL_TRANSACTIONS,
                getString(R.string.channel_transactions),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = getString(R.string.channel_transactions_desc) }
            mgr.createNotificationChannel(ch)
        }
    }

    companion object {
        const val CHANNEL_TRANSACTIONS = "transactions"
    }
}
