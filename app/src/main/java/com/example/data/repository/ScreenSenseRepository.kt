package com.example.data.repository

import android.content.Context
import com.example.data.ai.GeminiWellnessService
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryOverrideEntity
import com.example.data.local.HabitEntity
import com.example.data.local.SmallWinEntity
import com.example.data.local.UsageRecordEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.WellnessActionEntity
import com.example.data.model.AppCategory
import com.example.data.model.HabitDefaults
import com.example.data.model.SmallWin
import com.example.data.model.WellnessAction
import com.example.data.model.WellnessGoal
import com.example.data.usage.UsageStatsHelper
import com.example.domain.AnalysisEngine
import com.example.domain.RecommendationEngine
import com.example.domain.UsageAnalysisSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScreenSenseRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    val userPreferences: Flow<UserPreferences> = preferencesRepository.userPreferencesFlow

    val habitsFlow: Flow<List<HabitEntity>> = database.habitDao().getAllHabits()
    val smallWinsFlow: Flow<List<SmallWinEntity>> = database.smallWinDao().getAllSmallWins()

    fun getUsageForDate(date: String): Flow<List<UsageRecordEntity>> {
        return database.usageDao().getUsageForDate(date)
    }

    fun getActionForDate(date: String): Flow<WellnessAction?> {
        return database.actionDao().getActionForDate(date).map { entity ->
            entity?.let {
                WellnessAction(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    goal = it.goal,
                    effortLevel = it.effortLevel,
                    reason = it.reason,
                    confidence = it.confidence,
                    safetyNote = it.safetyNote,
                    alternatives = it.alternatives,
                    status = it.status,
                    date = it.date
                )
            }
        }
    }

    suspend fun initializeDefaultHabitsIfNeeded() = withContext(Dispatchers.IO) {
        val existing = database.habitDao().getAllHabits().first()
        if (existing.isEmpty()) {
            database.habitDao().insertHabits(HabitDefaults.getDefaultHabits())
        }
    }

    suspend fun refreshUsageData() = withContext(Dispatchers.IO) {
        val prefs = userPreferences.first()
        val hasPermission = UsageStatsHelper.hasUsageAccess(context)
        val today = UsageStatsHelper.getTodayDateString()

        val overrides = database.categoryOverrideDao().getAllOverridesDirect().associate { it.packageName to it.userCategory }

        if (hasPermission && !prefs.isSampleDataMode) {
            // Real usage mode: Purge old sample data so dummy entries never conflict
            database.usageDao().deleteSampleRecords()

            // Fetch actual on-device usage for today and past 6 days for rich trends
            val realRecords = mutableListOf<UsageRecordEntity>()
            for (dayOffset in 0..6) {
                val cal = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
                }
                val dayData = UsageStatsHelper.queryRealUsageForDay(context, cal, categoryOverrides = overrides)
                realRecords.addAll(dayData)
            }

            if (realRecords.isNotEmpty()) {
                database.usageDao().insertUsageRecords(realRecords)
            }
        } else if (prefs.isSampleDataMode) {
            val sampleRecords = UsageStatsHelper.getSampleProfilesData(prefs.sampleProfileId, daysCount = 7)
            if (sampleRecords.isNotEmpty()) {
                database.usageDao().insertUsageRecords(sampleRecords)
            }
        } else {
            // User requested real data but has not granted usage permission yet: purge sample data
            database.usageDao().deleteSampleRecords()
        }

        // Generate or ensure today's recommendation
        generateOrUpdateRecommendationForDate(today, prefs.selectedGoal, prefs.isAiPersonalizationEnabled)
    }

    suspend fun generateOrUpdateRecommendationForDate(
        date: String,
        goal: WellnessGoal,
        aiEnabled: Boolean
    ) = withContext(Dispatchers.IO) {
        val existingAction = database.actionDao().getActionForDate(date).first()
        if (existingAction != null && existingAction.status != "PENDING") {
            return@withContext // Do not overwrite user-acted state
        }

        val records = database.usageDao().getUsageForDateDirect(date)
        val analysis = AnalysisEngine.analyzeDailyUsage(records)
        var recommendation = RecommendationEngine.generateRecommendation(goal, analysis, date)

        if (aiEnabled) {
            recommendation = GeminiWellnessService.personalizeWellnessAction(recommendation, goal, analysis)
        }

        val entity = WellnessActionEntity(
            id = recommendation.id,
            title = recommendation.title,
            description = recommendation.description,
            goal = recommendation.goal,
            effortLevel = recommendation.effortLevel,
            reason = recommendation.reason,
            confidence = recommendation.confidence,
            safetyNote = recommendation.safetyNote,
            alternatives = recommendation.alternatives,
            status = existingAction?.status ?: "PENDING",
            date = date
        )
        database.actionDao().insertAction(entity)
    }

    suspend fun updateActionStatus(actionId: String, status: String, action: WellnessAction? = null) = withContext(Dispatchers.IO) {
        database.actionDao().updateActionStatus(actionId, status)
        if (status == "ACCEPTED" && action != null) {
            val win = SmallWinEntity(
                actionId = action.id,
                title = action.title,
                goal = action.goal,
                date = UsageStatsHelper.getTodayDateString(),
                status = "COMPLETED",
                note = "Completed: ${action.title}",
                reflection = "Took one small step toward ${action.goal.displayName}."
            )
            database.smallWinDao().insertSmallWin(win)
        }
    }

    suspend fun updateHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        database.habitDao().updateHabit(habit)
    }

    suspend fun logSmallWin(win: SmallWin) = withContext(Dispatchers.IO) {
        database.smallWinDao().insertSmallWin(
            SmallWinEntity(
                id = win.id,
                actionId = win.actionId,
                title = win.title,
                goal = win.goal,
                date = win.date,
                status = win.status,
                note = win.note,
                reflection = win.reflection
            )
        )
    }

    suspend fun deleteSmallWin(id: Long) = withContext(Dispatchers.IO) {
        database.smallWinDao().deleteSmallWin(id)
    }

    suspend fun updateAppCategory(packageName: String, category: AppCategory) = withContext(Dispatchers.IO) {
        database.categoryOverrideDao().insertOverride(CategoryOverrideEntity(packageName, category))
        database.usageDao().updateAppCategory(packageName, category)
    }

    suspend fun insertManualUsage(record: UsageRecordEntity) = withContext(Dispatchers.IO) {
        database.usageDao().insertUsageRecords(listOf(record))
        val prefs = userPreferences.first()
        generateOrUpdateRecommendationForDate(record.date, prefs.selectedGoal, prefs.isAiPersonalizationEnabled)
    }

    suspend fun importCsvData(csvText: String) = withContext(Dispatchers.IO) {
        val records = UsageStatsHelper.parseCsvUsage(csvText)
        if (records.isNotEmpty()) {
            database.usageDao().insertUsageRecords(records)
            val prefs = userPreferences.first()
            generateOrUpdateRecommendationForDate(records.first().date, prefs.selectedGoal, prefs.isAiPersonalizationEnabled)
        }
    }

    suspend fun importOcrTextData(ocrText: String) = withContext(Dispatchers.IO) {
        val records = UsageStatsHelper.parseExtractedScreenTimeText(ocrText)
        if (records.isNotEmpty()) {
            database.usageDao().insertUsageRecords(records)
            val prefs = userPreferences.first()
            generateOrUpdateRecommendationForDate(records.first().date, prefs.selectedGoal, prefs.isAiPersonalizationEnabled)
        }
    }

    suspend fun deleteAllUsageRecords() = withContext(Dispatchers.IO) {
        database.usageDao().deleteAllUsageRecords()
        database.actionDao().deleteAllActions()
    }

    suspend fun deleteAllScreenSenseData() = withContext(Dispatchers.IO) {
        database.usageDao().deleteAllUsageRecords()
        database.habitDao().deleteAllHabits()
        database.smallWinDao().deleteAllSmallWins()
        database.actionDao().deleteAllActions()
        database.categoryOverrideDao().deleteAllOverrides()
        preferencesRepository.clearAllPreferences()
        initializeDefaultHabitsIfNeeded()
    }

    // Preference setters
    suspend fun setOnboardingCompleted(completed: Boolean) = preferencesRepository.setOnboardingCompleted(completed)
    suspend fun setSelectedGoal(goal: WellnessGoal) = preferencesRepository.setSelectedGoal(goal)
    suspend fun setSampleDataMode(enabled: Boolean) = preferencesRepository.setSampleDataMode(enabled)
    suspend fun setSampleProfileId(profileId: String) = preferencesRepository.setSampleProfileId(profileId)
    suspend fun setAiPersonalizationEnabled(enabled: Boolean) = preferencesRepository.setAiPersonalizationEnabled(enabled)
    suspend fun setProgressTrackingMode(mode: String) = preferencesRepository.setProgressTrackingMode(mode)
    suspend fun setWeeklyTargetDays(days: Int) = preferencesRepository.setWeeklyTargetDays(days)
    suspend fun setQuotesEnabled(enabled: Boolean) = preferencesRepository.setQuotesEnabled(enabled)
    suspend fun setThemeMode(mode: String) = preferencesRepository.setThemeMode(mode)
    suspend fun setReducedMotion(reduced: Boolean) = preferencesRepository.setReducedMotion(reduced)
}
