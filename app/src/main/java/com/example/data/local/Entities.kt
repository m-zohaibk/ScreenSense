package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ActionEffort
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import com.example.data.model.WellnessGoal

@Entity(tableName = "usage_records", primaryKeys = ["packageName", "date"])
data class UsageRecordEntity(
    val packageName: String,
    val appLabel: String,
    val date: String, // yyyy-MM-dd
    val durationMillis: Long,
    val firstUsedMillis: Long?,
    val lastUsedMillis: Long?,
    val launchCount: Int?,
    val category: AppCategory,
    val confidence: String,
    val source: String
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean = false,
    val reminderEnabled: Boolean = false,
    val schedule: String = "Daily",
    val safetyNote: String? = null,
    val isPaused: Boolean = false,
    val isSkippedToday: Boolean = false,
    val goalHint: WellnessGoal? = null,
    val completedDates: List<String> = emptyList(),
    val streak: Int = 0
)

@Entity(tableName = "small_wins")
data class SmallWinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionId: String,
    val title: String,
    val goal: WellnessGoal,
    val date: String, // yyyy-MM-dd
    val status: String, // COMPLETED, PAUSED, RECOVERY, SKIPPED
    val note: String? = null,
    val reflection: String? = null
)

@Entity(tableName = "wellness_actions")
data class WellnessActionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val goal: WellnessGoal,
    val effortLevel: ActionEffort,
    val reason: String,
    val confidence: ConfidenceLevel,
    val safetyNote: String?,
    val alternatives: List<String>,
    val status: String, // PENDING, ACCEPTED, POSTPONED, DISMISSED
    val date: String
)

@Entity(tableName = "category_overrides")
data class CategoryOverrideEntity(
    @PrimaryKey val packageName: String,
    val userCategory: AppCategory
)

@Entity(tableName = "active_timers")
data class ActiveTimerEntity(
    @PrimaryKey val timerId: String, // Habit ID or action ID e.g. "habit_eye_rest"
    val title: String,
    val habitId: String?,
    val totalDurationSeconds: Int,
    val startTimestampMillis: Long,
    val targetEndTimestampMillis: Long,
    val remainingSeconds: Int,
    val isRunning: Boolean,
    val isPaused: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
