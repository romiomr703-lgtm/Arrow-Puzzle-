package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val completed: Boolean = false,
    val bestStars: Int = 0,
    val bestTimeSeconds: Int = 0,
    val completedAtMillis: Long = 0L,
)

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey val dateKey: String,
    val completed: Boolean = false,
    val stars: Int = 0,
    val bestTimeSeconds: Int = 0,
    val completedAtMillis: Long = 0L,
)
