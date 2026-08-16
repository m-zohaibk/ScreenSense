package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.WellnessGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screensense_prefs")

data class UserPreferences(
    val isOnboardingCompleted: Boolean,
    val selectedGoal: WellnessGoal,
    val isSampleDataMode: Boolean,
    val sampleProfileId: String,
    val isAiPersonalizationEnabled: Boolean,
    val progressTrackingMode: String, // "DAILY_WINS", "WEEKLY_TARGET", "NO_TRACKING", "RECOVERY_FRIENDLY"
    val weeklyTargetDays: Int,
    val isQuotesEnabled: Boolean,
    val themeMode: String, // "SYSTEM", "LIGHT", "DARK"
    val isReducedMotion: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_GOAL = stringPreferencesKey("selected_goal")
        val SAMPLE_DATA_MODE = booleanPreferencesKey("sample_data_mode")
        val SAMPLE_PROFILE_ID = stringPreferencesKey("sample_profile_id")
        val AI_PERSONALIZATION_ENABLED = booleanPreferencesKey("ai_personalization_enabled")
        val PROGRESS_TRACKING_MODE = stringPreferencesKey("progress_tracking_mode")
        val WEEKLY_TARGET_DAYS = intPreferencesKey("weekly_target_days")
        val QUOTES_ENABLED = booleanPreferencesKey("quotes_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val goalName = prefs[PreferencesKeys.SELECTED_GOAL] ?: WellnessGoal.DIGITAL_BALANCE.name
        val goal = runCatching { WellnessGoal.valueOf(goalName) }.getOrDefault(WellnessGoal.DIGITAL_BALANCE)

        UserPreferences(
            isOnboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            selectedGoal = goal,
            isSampleDataMode = prefs[PreferencesKeys.SAMPLE_DATA_MODE] ?: false,
            sampleProfileId = prefs[PreferencesKeys.SAMPLE_PROFILE_ID] ?: "CREATIVE_WORK",
            isAiPersonalizationEnabled = prefs[PreferencesKeys.AI_PERSONALIZATION_ENABLED] ?: false,
            progressTrackingMode = prefs[PreferencesKeys.PROGRESS_TRACKING_MODE] ?: "WEEKLY_TARGET",
            weeklyTargetDays = prefs[PreferencesKeys.WEEKLY_TARGET_DAYS] ?: 4,
            isQuotesEnabled = prefs[PreferencesKeys.QUOTES_ENABLED] ?: true,
            themeMode = prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            isReducedMotion = prefs[PreferencesKeys.REDUCED_MOTION] ?: false
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSelectedGoal(goal: WellnessGoal) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_GOAL] = goal.name
        }
    }

    suspend fun setSampleDataMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SAMPLE_DATA_MODE] = enabled
        }
    }

    suspend fun setSampleProfileId(profileId: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SAMPLE_PROFILE_ID] = profileId
        }
    }

    suspend fun setAiPersonalizationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AI_PERSONALIZATION_ENABLED] = enabled
        }
    }

    suspend fun setProgressTrackingMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PROGRESS_TRACKING_MODE] = mode
        }
    }

    suspend fun setWeeklyTargetDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.WEEKLY_TARGET_DAYS] = days
        }
    }

    suspend fun setQuotesEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.QUOTES_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.REDUCED_MOTION] = reduced
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
