package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HabitEntity
import com.example.data.local.SmallWinEntity
import com.example.data.local.UsageRecordEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.manager.HabitTimerManager
import com.example.data.manager.HabitTimerState
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import com.example.data.model.MotivationQuote
import com.example.data.model.MotivationQuotesData
import com.example.data.model.SmallWin
import com.example.data.model.WellnessAction
import com.example.data.model.WellnessGoal
import com.example.data.remote.GeminiAiService
import com.example.data.remote.GeminiAnalysisResult
import com.example.data.repository.ScreenSenseRepository
import com.example.data.usage.UsageStatsHelper
import com.example.domain.AnalysisEngine
import com.example.domain.UsageAnalysisSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScreenSenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScreenSenseRepository
    private val context = application.applicationContext
    val habitTimerManager = HabitTimerManager.getInstance(application)

    val userPreferences: StateFlow<UserPreferences>
    val todayUsageRecords: StateFlow<List<UsageRecordEntity>>
    val todayAction: StateFlow<WellnessAction?>
    val habits: StateFlow<List<HabitEntity>>
    val smallWins: StateFlow<List<SmallWinEntity>>
    val habitTimers: StateFlow<Map<String, HabitTimerState>> = habitTimerManager.timersState

    private val _hasUsageAccess = MutableStateFlow(false)
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess.asStateFlow()

    private val _quoteOffset = MutableStateFlow(0)
    val dailyQuote: StateFlow<MotivationQuote>

    val analysisSummary: StateFlow<UsageAnalysisSummary>

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _geminiResult = MutableStateFlow<GeminiAnalysisResult?>(null)
    val geminiResult: StateFlow<GeminiAnalysisResult?> = _geminiResult.asStateFlow()

    private val _aiStatusMessage = MutableStateFlow("✨ Gemini AI Ready")
    val aiStatusMessage: StateFlow<String> = _aiStatusMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        val prefsRepo = UserPreferencesRepository(application)
        repository = ScreenSenseRepository(application, db, prefsRepo)

        userPreferences = repository.userPreferences.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UserPreferences(
                isOnboardingCompleted = false,
                selectedGoal = WellnessGoal.DIGITAL_BALANCE,
                isSampleDataMode = false,
                sampleProfileId = "CREATIVE_WORK",
                isAiPersonalizationEnabled = true,
                progressTrackingMode = "WEEKLY_TARGET",
                weeklyTargetDays = 4,
                isQuotesEnabled = true,
                themeMode = "SYSTEM",
                isReducedMotion = false
            )
        )

        val todayStr = UsageStatsHelper.getTodayDateString()

        todayUsageRecords = repository.getUsageForDate(todayStr).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        todayAction = repository.getActionForDate(todayStr).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        habits = repository.habitsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        smallWins = repository.smallWinsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        analysisSummary = todayUsageRecords.mapToSummary()

        dailyQuote = combine(userPreferences, _quoteOffset) { prefs, offset ->
            MotivationQuotesData.getDailyQuote(prefs.selectedGoal, offset)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MotivationQuotesData.quotes.first()
        )

        checkUsageAccess()

        viewModelScope.launch {
            repository.initializeDefaultHabitsIfNeeded()
            repository.refreshUsageData()
        }

        // Trigger AI analysis when records or goal change
        viewModelScope.launch {
            combine(todayUsageRecords, userPreferences) { records, prefs ->
                Pair(records, prefs.selectedGoal)
            }.collect { (records, goal) ->
                if (records.isNotEmpty()) {
                    triggerGeminiAnalysis(records, goal)
                }
            }
        }
    }

    fun triggerGeminiAnalysis(
        records: List<UsageRecordEntity> = todayUsageRecords.value,
        goal: WellnessGoal = userPreferences.value.selectedGoal
    ) {
        if (records.isEmpty()) return
        viewModelScope.launch {
            _isAiGenerating.value = true
            _aiStatusMessage.value = "✨ Gemini AI analyzing telemetry..."
            val totalMins = records.sumOf { it.durationMillis } / (1000 * 60)
            val durFormatted = if (totalMins >= 60) "${totalMins / 60}h ${totalMins % 60}m" else "${totalMins}m"
            val todayStr = UsageStatsHelper.getTodayDateString()

            val result = GeminiAiService.generateCustomAiInsights(
                goal = goal,
                records = records,
                totalDurationFormatted = durFormatted,
                todayDate = todayStr
            )

            if (result != null) {
                _geminiResult.value = result
                _aiStatusMessage.value = "✨ Gemini AI Live Analysis"
            } else {
                _aiStatusMessage.value = "✨ Dynamic Behavioral AI Engine"
            }
            _isAiGenerating.value = false
        }
    }

    fun regenerateAiInsights() {
        triggerGeminiAnalysis()
    }

    private fun StateFlow<List<UsageRecordEntity>>.mapToSummary(): StateFlow<UsageAnalysisSummary> {
        return combine(this) { records ->
            AnalysisEngine.analyzeDailyUsage(records[0])
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AnalysisEngine.analyzeDailyUsage(emptyList())
        )
    }

    fun checkUsageAccess() {
        val hasAccess = UsageStatsHelper.hasUsageAccess(context)
        _hasUsageAccess.value = hasAccess
    }

    fun refreshUsage() {
        viewModelScope.launch {
            checkUsageAccess()
            repository.refreshUsageData()
        }
    }

    fun selectGoal(goal: WellnessGoal) {
        viewModelScope.launch {
            repository.setSelectedGoal(goal)
            val today = UsageStatsHelper.getTodayDateString()
            repository.generateOrUpdateRecommendationForDate(
                today,
                goal,
                userPreferences.value.isAiPersonalizationEnabled
            )
        }
    }

    fun completeOnboarding(useSample: Boolean) {
        viewModelScope.launch {
            repository.setSampleDataMode(useSample)
            repository.setOnboardingCompleted(true)
            repository.refreshUsageData()
        }
    }

    fun acceptAction(action: WellnessAction) {
        viewModelScope.launch {
            repository.updateActionStatus(action.id, "ACCEPTED", action)
        }
    }

    fun postponeAction(actionId: String) {
        viewModelScope.launch {
            repository.updateActionStatus(actionId, "POSTPONED")
        }
    }

    fun dismissAction(actionId: String) {
        viewModelScope.launch {
            repository.updateActionStatus(actionId, "DISMISSED")
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(enabled = !habit.enabled))
        }
    }

    fun addHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(enabled = true, isPaused = false, isSkippedToday = false))
        }
    }

    fun toggleHabitCompletion(habit: HabitEntity) {
        viewModelScope.launch {
            val todayStr = UsageStatsHelper.getTodayDateString()
            val isAlreadyCompleted = habit.completedDates.contains(todayStr)
            val updatedCompletedDates = if (isAlreadyCompleted) {
                habit.completedDates.filter { it != todayStr }
            } else {
                habit.completedDates + todayStr
            }
            val updatedStreak = if (isAlreadyCompleted) {
                maxOf(0, habit.streak - 1)
            } else {
                habit.streak + 1
            }

            val updatedHabit = habit.copy(
                completedDates = updatedCompletedDates,
                streak = updatedStreak,
                isSkippedToday = false
            )
            repository.updateHabit(updatedHabit)

            if (!isAlreadyCompleted) {
                repository.logSmallWin(
                    SmallWin(
                        actionId = "habit_${habit.id}_$todayStr",
                        title = habit.name,
                        goal = habit.goalHint ?: userPreferences.value.selectedGoal,
                        date = todayStr,
                        status = "COMPLETED",
                        note = "Completed daily habit: ${habit.name}",
                        reflection = "Daily check completed for ${habit.schedule}"
                    )
                )
            }
        }
    }

    fun pauseHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isPaused = !habit.isPaused))
        }
    }

    fun skipHabitToday(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isSkippedToday = !habit.isSkippedToday))
        }
    }

    fun startHabitTimer(habitId: String, totalSeconds: Int) {
        habitTimerManager.startTimer(habitId, totalSeconds)
    }

    fun pauseHabitTimer(habitId: String, totalSeconds: Int) {
        habitTimerManager.pauseTimer(habitId, totalSeconds)
    }

    fun resetHabitTimer(habitId: String, totalSeconds: Int) {
        habitTimerManager.resetTimer(habitId, totalSeconds)
    }

    fun logCustomSmallWin(title: String, goal: WellnessGoal, status: String, reflection: String?) {
        viewModelScope.launch {
            repository.logSmallWin(
                SmallWin(
                    actionId = "custom_${System.currentTimeMillis()}",
                    title = title,
                    goal = goal,
                    date = UsageStatsHelper.getTodayDateString(),
                    status = status,
                    note = "Logged small win",
                    reflection = reflection
                )
            )
        }
    }

    fun deleteSmallWin(id: Long) {
        viewModelScope.launch {
            repository.deleteSmallWin(id)
        }
    }

    fun updateAppCategory(packageName: String, category: AppCategory) {
        viewModelScope.launch {
            repository.updateAppCategory(packageName, category)
            repository.refreshUsageData()
        }
    }

    fun setSampleProfile(profileId: String) {
        viewModelScope.launch {
            repository.setSampleProfileId(profileId)
            repository.setSampleDataMode(true)
            repository.refreshUsageData()
        }
    }

    fun setSampleDataMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSampleDataMode(enabled)
            repository.refreshUsageData()
        }
    }

    fun addManualUsage(appLabel: String, category: AppCategory, durationMinutes: Long) {
        viewModelScope.launch {
            val today = UsageStatsHelper.getTodayDateString()
            val pkg = "manual." + appLabel.lowercase().replace(" ", ".")
            val record = UsageRecordEntity(
                packageName = pkg,
                appLabel = appLabel,
                date = today,
                durationMillis = durationMinutes * 60 * 1000L,
                firstUsedMillis = System.currentTimeMillis() - durationMinutes * 60 * 1000L,
                lastUsedMillis = System.currentTimeMillis(),
                launchCount = 1,
                category = category,
                confidence = ConfidenceLevel.HIGH.name,
                source = "MANUAL"
            )
            repository.insertManualUsage(record)
        }
    }

    fun importCsv(csvContent: String) {
        viewModelScope.launch {
            repository.importCsvData(csvContent)
        }
    }

    fun importOcrText(ocrText: String) {
        viewModelScope.launch {
            repository.importOcrTextData(ocrText)
        }
    }

    fun deleteAllUsageData() {
        viewModelScope.launch {
            repository.deleteAllUsageRecords()
            repository.refreshUsageData()
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllScreenSenseData()
        }
    }

    fun refreshQuote() {
        _quoteOffset.value += 1
    }

    fun setProgressTrackingMode(mode: String) {
        viewModelScope.launch {
            repository.setProgressTrackingMode(mode)
        }
    }

    fun setWeeklyTargetDays(days: Int) {
        viewModelScope.launch {
            repository.setWeeklyTargetDays(days)
        }
    }

    fun setQuotesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setQuotesEnabled(enabled)
        }
    }

    fun setAiPersonalization(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAiPersonalizationEnabled(enabled)
            val today = UsageStatsHelper.getTodayDateString()
            repository.generateOrUpdateRecommendationForDate(
                today,
                userPreferences.value.selectedGoal,
                enabled
            )
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setReducedMotion(reduced: Boolean) {
        viewModelScope.launch {
            repository.setReducedMotion(reduced)
        }
    }
}
