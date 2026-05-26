package com.coinkarma.app.data.quests

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Query("DELETE FROM custom_quests")
    suspend fun deleteAll()
}
