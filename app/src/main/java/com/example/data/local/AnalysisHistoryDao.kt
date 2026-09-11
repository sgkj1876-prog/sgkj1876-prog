package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisHistoryDao {
    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<AnalysisHistoryEntity>>

    @Query("SELECT * FROM analysis_history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: String): AnalysisHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: AnalysisHistoryEntity)

    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM analysis_history")
    suspend fun clearAllHistory()
}
