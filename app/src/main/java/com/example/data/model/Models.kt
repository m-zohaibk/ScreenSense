package com.example.data.model

enum class WellnessGoal(
    val displayName: String,
    val description: String,
    val iconName: String
) {
    SLEEP(
        displayName = "Better sleep routine",
        description = "Gentle wind-down and screen-free pauses before bedtime.",
        iconName = "bedtime"
    ),
    FOCUS(
        displayName = "Better focus",
        description = "Understand checking patterns and carve out uninterrupted flow.",
        iconName = "center_focus_strong"
    ),
    LESS_STRESS(
        displayName = "Less stress",
        description = "Mindful breathing and screen pauses during extended sessions.",
        iconName = "spa"
    ),
    MOVEMENT(
        displayName = "More movement",
        description = "Frequent posture checks, stretches, and refreshing walk pauses.",
        iconName = "directions_walk"
    ),
    OFFLINE_CONNECTION(
        displayName = "More offline connection",
        description = "Nurture in-person relationships, hobbies, and nature time.",
        iconName = "people"
    ),
    DIGITAL_BALANCE(
        displayName = "Healthier digital balance",
        description = "Distinguish essential digital tasks from unintended scrolling.",
        iconName = "balance"
    )
}

enum class AppCategory(
    val displayName: String,
    val colorHex: Long
) {
    DEVELOPMENT("Development", 0xFF6366F1),
    WORK_STUDY("Work & Study", 0xFF0D9488),
    TOOLS("Tools", 0xFFF59E0B),
    ENTERTAINMENT("Entertainment", 0xFFEC4899),
    SOCIAL_COMMUNICATION("Social & Chat", 0xFF8B5CF6),
    BROWSING("Browsing & News", 0xFF3B82F6),
    HEALTH_WELLNESS("Health & Wellness", 0xFF10B981),
    UNKNOWN("Other", 0xFF64748B);

    companion object {
        fun fromLegacy(name: String?): AppCategory {
            if (name == null) return UNKNOWN
            return when (name.uppercase().replace(" ", "_")) {
                "DEVELOPMENT", "DEV" -> DEVELOPMENT
                "WORK_STUDY", "WORK", "STUDY" -> WORK_STUDY
                "TOOLS", "TOOLS_UTILITY", "TOOL", "UTILITIES", "ACCESSIBILITY" -> TOOLS
                "ENTERTAINMENT", "MEDIA", "GAMES" -> ENTERTAINMENT
                "SOCIAL_COMMUNICATION", "SOCIAL", "CHAT", "COMMUNICATION" -> SOCIAL_COMMUNICATION
                "BROWSING", "NEWS", "READING" -> BROWSING
                "HEALTH_WELLNESS", "HEALTH", "FITNESS", "MINDFULNESS" -> HEALTH_WELLNESS
                else -> runCatching { valueOf(name.uppercase().replace(" ", "_")) }.getOrDefault(UNKNOWN)
            }
        }
    }
}

enum class ConfidenceLevel(
    val label: String,
    val description: String
) {
    HIGH(
        label = "High confidence",
        description = "Directly calculated from available usage data."
    ),
    MEDIUM(
        label = "Medium confidence",
        description = "Depends on app categorization or incomplete event data."
    ),
    LOW(
        label = "Low confidence",
        description = "A possible interpretation requiring user confirmation."
    ),
    UNKNOWN(
        label = "Unknown",
        description = "Cannot be determined from the available data."
    )
}

data class AppUsageRecord(
    val packageName: String,
    val appLabel: String,
    val date: String, // Format: yyyy-MM-dd
    val durationMillis: Long,
    val firstUsedMillis: Long? = null,
    val lastUsedMillis: Long? = null,
    val launchCount: Int? = null,
    val category: AppCategory = AppCategory.UNKNOWN,
    val confidence: String = ConfidenceLevel.HIGH.name,
    val source: String = "SYSTEM" // "SYSTEM", "SAMPLE", "MANUAL", "CSV", "OCR"
)

data class PatternCard(
    val id: String,
    val title: String,
    val description: String,
    val category: AppCategory? = null,
    val confidence: ConfidenceLevel = ConfidenceLevel.HIGH,
    val limitationNote: String? = null,
    val highlightMetric: String? = null,
    val isDismissed: Boolean = false
)

enum class ActionEffort(
    val label: String,
    val durationText: String
) {
    TINY("Tiny", "1–3 minutes"),
    MEDIUM("Medium", "10–20 minutes"),
    FLEXIBLE("Flexible", "Choose your own pace")
}

data class WellnessAction(
    val id: String,
    val title: String,
    val description: String,
    val goal: WellnessGoal,
    val effortLevel: ActionEffort = ActionEffort.TINY,
    val reason: String,
    val confidence: ConfidenceLevel = ConfidenceLevel.HIGH,
    val safetyNote: String? = null,
    val alternatives: List<String> = emptyList(),
    val status: String = "PENDING", // PENDING, ACCEPTED, POSTPONED, DISMISSED
    val date: String = ""
)

data class Habit(
    val id: String,
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

data class SmallWin(
    val id: Long = 0,
    val actionId: String,
    val title: String,
    val goal: WellnessGoal,
    val date: String, // yyyy-MM-dd
    val status: String, // "COMPLETED", "PAUSED", "RECOVERY", "SKIPPED"
    val note: String? = null,
    val reflection: String? = null
)

data class MotivationQuote(
    val id: String,
    val quote: String,
    val goal: WellnessGoal,
    val theme: String,
    val author: String = "ScreenSense Mindfulness"
)
