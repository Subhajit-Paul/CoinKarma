package com.coinkarma.app.sms

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coinkarma.app.CoinKarmaApp
import com.coinkarma.app.MainActivity
import com.coinkarma.app.R
import com.coinkarma.app.data.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives SMS, parses it, persists to Room, fires a notification.
 *
 * PERMISSIONS:
 *   RECEIVE_SMS + READ_SMS — request at runtime on the Profile or onboarding screen.
 *   POST_NOTIFICATIONS   — Android 13+, request at runtime too.
 */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val parsed = SmsParser.parse(fullBody) ?: return

        val app = context.applicationContext as CoinKarmaApp
        val dao = app.db.transactions()

        CoroutineScope(Dispatchers.IO).launch {
            val id = dao.insert(
                TransactionEntity(
                    amount = parsed.amount,
                    category = parsed.guessedCategory,
                    note = parsed.merchant,
                    merchant = parsed.merchant,
                    timestamp = System.currentTimeMillis(),
                    source = "sms",
                    smsRaw = parsed.raw,
                )
            )
            notifyUser(context, parsed, id)
        }
    }

    private fun notifyUser(context: Context, parsed: SmsParser.Parsed, txId: Long) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                putExtra("openTransaction", txId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notif = NotificationCompat.Builder(context, CoinKarmaApp.CHANNEL_TRANSACTIONS)
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // TODO: swap for ck icon
            .setContentTitle("Spend detected · ₹${parsed.amount.toInt()}")
            .setContentText(
                (parsed.merchant?.let { "$it · " } ?: "") +
                        parsed.guessedCategory.replaceFirstChar { it.uppercase() }
            )
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(txId.toInt(), notif)
    }
}
