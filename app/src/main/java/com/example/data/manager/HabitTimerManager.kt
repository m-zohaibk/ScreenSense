package com.example.data.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HabitTimerState(
    val habitId: String,
    val totalSeconds: Int,
    val secondsRemaining: Int,
    val isRunning: Boolean,
    val isFinished: Boolean = false
)

class HabitTimerManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("habit_timers_prefs", Context.MODE_PRIVATE)

    private val _timersState = MutableStateFlow<Map<String, HabitTimerState>>(emptyMap())
    val timersState: StateFlow<Map<String, HabitTimerState>> = _timersState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Start background ticker loop that updates all active running timers
        scope.launch {
            while (isActive) {
                updateAllRunningTimers()
                delay(1000L)
            }
        }
    }

    private fun getEndTime(habitId: String): Long {
        return prefs.getLong("timer_${habitId}_end_time", 0L)
    }

    private fun getSavedRemaining(habitId: String, defaultSeconds: Int): Int {
        return prefs.getInt("timer_${habitId}_remaining", defaultSeconds)
    }

    private fun getSavedTotal(habitId: String, defaultSeconds: Int): Int {
        return prefs.getInt("timer_${habitId}_total", defaultSeconds)
    }

    private fun getSavedIsRunning(habitId: String): Boolean {
        return prefs.getBoolean("timer_${habitId}_running", false)
    }

    fun getOrInitTimerState(habitId: String, defaultSeconds: Int): HabitTimerState {
        val total = getSavedTotal(habitId, defaultSeconds)
        val isRunning = getSavedIsRunning(habitId)
        val endTime = getEndTime(habitId)

        val currentRemaining: Int
        val finished: Boolean
        val runningNow: Boolean

        if (isRunning) {
            val now = System.currentTimeMillis()
            val diffSeconds = ((endTime - now) / 1000L).toInt()
            if (diffSeconds <= 0) {
                currentRemaining = 0
                finished = true
                runningNow = false
                // Auto finish in prefs
                prefs.edit()
                    .putBoolean("timer_${habitId}_running", false)
                    .putInt("timer_${habitId}_remaining", 0)
                    .apply()
            } else {
                currentRemaining = diffSeconds
                finished = false
                runningNow = true
            }
        } else {
            currentRemaining = getSavedRemaining(habitId, defaultSeconds)
            finished = false
            runningNow = false
        }

        val state = HabitTimerState(
            habitId = habitId,
            totalSeconds = total,
            secondsRemaining = currentRemaining,
            isRunning = runningNow,
            isFinished = finished
        )

        val currentMap = _timersState.value.toMutableMap()
        currentMap[habitId] = state
        _timersState.value = currentMap

        return state
    }

    fun startTimer(habitId: String, defaultTotalSeconds: Int) {
        val currentState = getOrInitTimerState(habitId, defaultTotalSeconds)
        val secondsToRun = if (currentState.secondsRemaining > 0) currentState.secondsRemaining else defaultTotalSeconds
        val endTime = System.currentTimeMillis() + (secondsToRun * 1000L)

        prefs.edit()
            .putLong("timer_${habitId}_end_time", endTime)
            .putInt("timer_${habitId}_total", defaultTotalSeconds)
            .putInt("timer_${habitId}_remaining", secondsToRun)
            .putBoolean("timer_${habitId}_running", true)
            .apply()

        val newState = HabitTimerState(
            habitId = habitId,
            totalSeconds = defaultTotalSeconds,
            secondsRemaining = secondsToRun,
            isRunning = true,
            isFinished = false
        )
        val currentMap = _timersState.value.toMutableMap()
        currentMap[habitId] = newState
        _timersState.value = currentMap
    }

    fun pauseTimer(habitId: String, defaultTotalSeconds: Int) {
        val currentState = getOrInitTimerState(habitId, defaultTotalSeconds)
        val remaining = currentState.secondsRemaining

        prefs.edit()
            .putLong("timer_${habitId}_end_time", 0L)
            .putInt("timer_${habitId}_remaining", remaining)
            .putBoolean("timer_${habitId}_running", false)
            .apply()

        val newState = HabitTimerState(
            habitId = habitId,
            totalSeconds = defaultTotalSeconds,
            secondsRemaining = remaining,
            isRunning = false,
            isFinished = false
        )
        val currentMap = _timersState.value.toMutableMap()
        currentMap[habitId] = newState
        _timersState.value = currentMap
    }

    fun resetTimer(habitId: String, defaultTotalSeconds: Int) {
        prefs.edit()
            .putLong("timer_${habitId}_end_time", 0L)
            .putInt("timer_${habitId}_total", defaultTotalSeconds)
            .putInt("timer_${habitId}_remaining", defaultTotalSeconds)
            .putBoolean("timer_${habitId}_running", false)
            .apply()

        val newState = HabitTimerState(
            habitId = habitId,
            totalSeconds = defaultTotalSeconds,
            secondsRemaining = defaultTotalSeconds,
            isRunning = false,
            isFinished = false
        )
        val currentMap = _timersState.value.toMutableMap()
        currentMap[habitId] = newState
        _timersState.value = currentMap
    }

    private fun updateAllRunningTimers() {
        val keys = prefs.all.keys
        val habitIds = keys.filter { it.startsWith("timer_") && it.endsWith("_running") }
            .map { it.removePrefix("timer_").removeSuffix("_running") }

        if (habitIds.isEmpty()) return

        val updatedMap = _timersState.value.toMutableMap()
        var hasChanged = false

        for (habitId in habitIds) {
            val isRunning = prefs.getBoolean("timer_${habitId}_running", false)
            if (isRunning) {
                val endTime = prefs.getLong("timer_${habitId}_end_time", 0L)
                val total = prefs.getInt("timer_${habitId}_total", 60)
                val now = System.currentTimeMillis()
                val diffSeconds = ((endTime - now) / 1000L).toInt()

                if (diffSeconds <= 0) {
                    prefs.edit()
                        .putBoolean("timer_${habitId}_running", false)
                        .putInt("timer_${habitId}_remaining", 0)
                        .apply()

                    updatedMap[habitId] = HabitTimerState(
                        habitId = habitId,
                        totalSeconds = total,
                        secondsRemaining = 0,
                        isRunning = false,
                        isFinished = true
                    )
                    hasChanged = true
                } else {
                    val prev = updatedMap[habitId]
                    if (prev?.secondsRemaining != diffSeconds || !prev.isRunning) {
                        updatedMap[habitId] = HabitTimerState(
                            habitId = habitId,
                            totalSeconds = total,
                            secondsRemaining = diffSeconds,
                            isRunning = true,
                            isFinished = false
                        )
                        hasChanged = true
                    }
                }
            }
        }

        if (hasChanged) {
            _timersState.value = updatedMap
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HabitTimerManager? = null

        fun getInstance(context: Context): HabitTimerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HabitTimerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
