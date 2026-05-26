package com.coinkarma.app.data.transactions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Spend event. One row per logged transaction (manual or SMS-parsed).
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "category") val category: String,  // "food" | "transport" | ...
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "merchant") val merchant: String? = null,
    @ColumnInfo(name = "timestamp") val timestamp: Long,  // epoch millis
    @ColumnInfo(name = "source") val source: String = "manual", // "manual" | "sms"
    @ColumnInfo(name = "sms_raw") val smsRaw: String? = null,
)
