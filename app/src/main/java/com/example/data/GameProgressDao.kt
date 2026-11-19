package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProgressDao {

    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun observeAllLevelProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getLevelProgress(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevelProgress(progress: LevelProgressEntity)

    @Query("SELECT * FROM daily_progress ORDER BY dateKey DESC")
    fun observeAllDailyProgress(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyProgress(dateKey: String): DailyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyProgress(progress: DailyProgressEntity)

    @Query("DELETE FROM level_progress")
    suspend fun clearAllLevelProgress()

    @Query("DELETE FROM daily_progress")
    suspend fun clearAllDailyProgress()
}
