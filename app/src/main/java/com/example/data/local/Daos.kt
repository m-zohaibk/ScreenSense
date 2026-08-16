package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage_records WHERE date = :date ORDER BY durationMillis DESC")
    fun getUsageForDate(date: String): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE date = :date ORDER BY durationMillis DESC")
    suspend fun getUsageForDateDirect(date: String): List<UsageRecordEntity>

    @Query("SELECT * FROM usage_records ORDER BY date DESC")
    fun getAllUsageRecords(): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE date >= :startDate ORDER BY date DESC")
    fun getUsageSinceDate(startDate: String): Flow<List<UsageRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecords(records: List<UsageRecordEntity>)

    @Query("UPDATE usage_records SET category = :category WHERE packageName = :packageName")
    suspend fun updateAppCategory(packageName: String, category: com.example.data.model.AppCategory)

    @Query("DELETE FROM usage_records WHERE source = 'SAMPLE'")
    suspend fun deleteSampleRecords()

    @Query("DELETE FROM usage_records")
    suspend fun deleteAllUsageRecords()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY name ASC")
    suspend fun getAllHabitsDirect(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitDirect(id: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE enabled = 1")
    fun getEnabledHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}

@Dao
interface SmallWinDao {
    @Query("SELECT * FROM small_wins ORDER BY date DESC, id DESC")
    fun getAllSmallWins(): Flow<List<SmallWinEntity>>

    @Query("SELECT * FROM small_wins WHERE date >= :startDate ORDER BY date DESC")
    fun getSmallWinsSince(startDate: String): Flow<List<SmallWinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmallWin(win: SmallWinEntity)

    @Query("DELETE FROM small_wins WHERE id = :id")
    suspend fun deleteSmallWin(id: Long)

    @Query("DELETE FROM small_wins")
    suspend fun deleteAllSmallWins()
}

@Dao
interface ActionDao {
    @Query("SELECT * FROM wellness_actions WHERE date = :date LIMIT 1")
    fun getActionForDate(date: String): Flow<WellnessActionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: WellnessActionEntity)

    @Query("UPDATE wellness_actions SET status = :status WHERE id = :id")
    suspend fun updateActionStatus(id: String, status: String)

    @Query("DELETE FROM wellness_actions")
    suspend fun deleteAllActions()
}

@Dao
interface CategoryOverrideDao {
    @Query("SELECT * FROM category_overrides")
    fun getAllOverrides(): Flow<List<CategoryOverrideEntity>>

    @Query("SELECT * FROM category_overrides")
    suspend fun getAllOverridesDirect(): List<CategoryOverrideEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: CategoryOverrideEntity)

    @Query("DELETE FROM category_overrides")
    suspend fun deleteAllOverrides()
}

@Dao
interface TimerDao {
    @Query("SELECT * FROM active_timers")
    fun getAllActiveTimers(): Flow<List<ActiveTimerEntity>>

    @Query("SELECT * FROM active_timers")
    suspend fun getAllActiveTimersDirect(): List<ActiveTimerEntity>

    @Query("SELECT * FROM active_timers WHERE timerId = :timerId LIMIT 1")
    fun getTimer(timerId: String): Flow<ActiveTimerEntity?>

    @Query("SELECT * FROM active_timers WHERE timerId = :timerId LIMIT 1")
    suspend fun getTimerDirect(timerId: String): ActiveTimerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTimer(timer: ActiveTimerEntity)

    @Query("DELETE FROM active_timers WHERE timerId = :timerId")
    suspend fun deleteTimer(timerId: String)

    @Query("DELETE FROM active_timers")
    suspend fun deleteAllTimers()
}
