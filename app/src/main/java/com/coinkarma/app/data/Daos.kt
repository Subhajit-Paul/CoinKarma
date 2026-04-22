package com.coinkarma.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun observeRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp BETWEEN :start AND :end")
    fun sumRange(start: Long, end: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity): Long

    @Delete
    suspend fun delete(tx: TransactionEntity)
}

@Dao
interface CustomQuestDao {
    @Query("SELECT * FROM custom_quests ORDER BY id DESC")
    fun observeAll(): Flow<List<CustomQuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(q: CustomQuestEntity): Long

    @Update
    suspend fun update(q: CustomQuestEntity)

    @Query("DELETE FROM custom_quests WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)
}
