package com.example.domain

import com.example.data.local.UsageRecordEntity
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import com.example.data.model.PatternCard
import java.util.Calendar

data class AppTelemetryInsight(
    val packageName: String,
    val appLabel: String,
    val category: AppCategory,
    val durationMillis: Long,
    val formattedDuration: String,
    val percentageOfTotal: Float,
    val launchCount: Int,
    val cognitiveImpactLevel: String, // "High Drain", "Moderate Drain", "Productive Flow", "Neutral"
    val analysisReason: String, // Valid behavioral / telemetry reason mentioning the app name
    val actionableTakeaway: String
)

data class RealtimeAiTelemetryReport(
    val headline: String,
    val overallSummary: String,
    val cognitiveLoadLabel: String,
    val cognitiveLoadScore: Int, // 0 - 100
    val topAppInsights: List<AppTelemetryInsight>,
    val telemetryKeyFindings: List<String>,
    val productivityRatio: Float,
    val contextSwitchingFrequency: String // "High", "Moderate", "Calm"
)

data class UsageAnalysisSummary(
    val totalDurationMillis: Long,
    val formattedTotalTime: String,
    val topApp: UsageRecordEntity?,
    val categoryBreakdown: Map<AppCategory, Long>,
    val categoryPercentages: Map<AppCategory, Float>,
    val totalLaunches: Int,
    val patternCards: List<PatternCard>,
    val dominantCategory: AppCategory,
    val hasEveningUsage: Boolean,
    val hasFrequentChecking: Boolean,
    val hasLongSession: Boolean,
    val dataCompletenessText: String,
    val realtimeReport: RealtimeAiTelemetryReport
)

object AnalysisEngine {

    fun analyzeDailyUsage(records: List<UsageRecordEntity>): UsageAnalysisSummary {
        if (records.isEmpty()) {
            val emptyReport = RealtimeAiTelemetryReport(
                headline = "No Active Usage Telemetry",
                overallSummary = "Connect Usage Access or explore Sample Profiles to see real-time app telemetry and behavioral AI analysis.",
                cognitiveLoadLabel = "Restful & Clear",
                cognitiveLoadScore = 15,
                topAppInsights = emptyList(),
                telemetryKeyFindings = listOf(
                    "No app durations recorded yet today.",
                    "Grant Usage Access to enable live AI analysis with app-specific reasons."
                ),
                productivityRatio = 0f,
                contextSwitchingFrequency = "Calm"
            )
            return UsageAnalysisSummary(
                totalDurationMillis = 0L,
                formattedTotalTime = "0m",
                topApp = null,
                categoryBreakdown = emptyMap(),
                categoryPercentages = emptyMap(),
                totalLaunches = 0,
                patternCards = listOf(
                    PatternCard(
                        id = "no_data",
                        title = "No screen data recorded yet today",
                        description = "When usage data is available, ScreenSense will highlight one meaningful pattern here.",
                        confidence = ConfidenceLevel.UNKNOWN,
                        limitationNote = "Connect Usage Access or explore Sample Profiles in Data & Privacy."
                    )
                ),
                dominantCategory = AppCategory.UNKNOWN,
                hasEveningUsage = false,
                hasFrequentChecking = false,
                hasLongSession = false,
                dataCompletenessText = "No visible records",
                realtimeReport = emptyReport
            )
        }

        val totalDuration = records.sumOf { it.durationMillis }
        val topApp = records.maxByOrNull { it.durationMillis }

        val categoryMap = mutableMapOf<AppCategory, Long>()
        for (r in records) {
            categoryMap[r.category] = (categoryMap[r.category] ?: 0L) + r.durationMillis
        }

        val categoryPercentages = categoryMap.mapValues { (_, duration) ->
            if (totalDuration > 0) (duration.toFloat() / totalDuration) * 100f else 0f
        }

        val dominantCategory = categoryMap.maxByOrNull { it.value }?.key ?: AppCategory.UNKNOWN
        val totalLaunches = records.sumOf { it.launchCount ?: 0 }

        // Check evening usage (after 9 PM / 21:00)
        var eveningCount = 0
        var frequentCheckingApp: UsageRecordEntity? = null
        var longSessionApp: UsageRecordEntity? = null

        for (r in records) {
            if (r.lastUsedMillis != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = r.lastUsedMillis }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour >= 21 || hour < 4) {
                    eveningCount++
                }
            }
            if ((r.launchCount ?: 0) >= 15 && (r.durationMillis / (r.launchCount ?: 1)) < (3 * 60 * 1000)) {
                frequentCheckingApp = r
            }
            if (r.durationMillis >= 60 * 60 * 1000) { // 60+ minutes in single app
                longSessionApp = r
            }
        }

        val patterns = mutableListOf<PatternCard>()

        // 1. Dominant category pattern
        val dominantPercent = categoryPercentages[dominantCategory] ?: 0f
        if (dominantCategory == AppCategory.WORK_STUDY || dominantCategory == AppCategory.SOCIAL_COMMUNICATION) {
            patterns.add(
                PatternCard(
                    id = "p_productive",
                    title = "Most visible usage was ${dominantCategory.displayName.lowercase()}",
                    description = "${dominantPercent.toInt()}% of today's screen time was dedicated to essential tasks, learning, or communication.",
                    category = dominantCategory,
                    confidence = ConfidenceLevel.HIGH,
                    limitationNote = "ScreenSense does not treat work or communication screen time as wasted.",
                    highlightMetric = "${dominantPercent.toInt()}% ${dominantCategory.displayName}"
                )
            )
        } else if (dominantCategory == AppCategory.ENTERTAINMENT) {
            patterns.add(
                PatternCard(
                    id = "p_entertainment",
                    title = "Entertainment apps were a large part of today's visible usage",
                    description = "About ${dominantPercent.toInt()}% of today's visible screen time was in entertainment media like video or gaming.",
                    category = dominantCategory,
                    confidence = ConfidenceLevel.HIGH,
                    limitationNote = "Duration shows time spent, but not whether it was restorative or passive.",
                    highlightMetric = "${dominantPercent.toInt()}% Entertainment"
                )
            )
        } else if (dominantCategory == AppCategory.BROWSING) {
            patterns.add(
                PatternCard(
                    id = "p_browsing",
                    title = "Active browsing was a primary pattern today",
                    description = "Browser apps accounted for ${dominantPercent.toInt()}% of total screen time.",
                    category = dominantCategory,
                    confidence = ConfidenceLevel.MEDIUM,
                    limitationNote = "ScreenSense does not know what you were browsing (reading, research, shopping, or relaxing).",
                    highlightMetric = "${dominantPercent.toInt()}% Browsing"
                )
            )
        }

        // 2. Frequent short checking pattern
        if (frequentCheckingApp != null) {
            patterns.add(
                PatternCard(
                    id = "p_checking",
                    title = "Frequent check-in pattern on ${frequentCheckingApp.appLabel}",
                    description = "You opened ${frequentCheckingApp.appLabel} ${frequentCheckingApp.launchCount} times for short sessions.",
                    category = frequentCheckingApp.category,
                    confidence = ConfidenceLevel.HIGH,
                    limitationNote = "Frequent opens often occur during transition moments or notifications.",
                    highlightMetric = "${frequentCheckingApp.launchCount} opens"
                )
            )
        }

        // 3. Long continuous session pattern
        if (longSessionApp != null) {
            val hours = longSessionApp.durationMillis / (1000 * 60 * 60)
            val mins = (longSessionApp.durationMillis / (1000 * 60)) % 60
            patterns.add(
                PatternCard(
                    id = "p_long_session",
                    title = "Extended session on ${longSessionApp.appLabel}",
                    description = "You spent ${if (hours > 0) "${hours}h " else ""}${mins}m in ${longSessionApp.appLabel}.",
                    category = longSessionApp.category,
                    confidence = ConfidenceLevel.HIGH,
                    limitationNote = "Extended focus is often necessary; brief posture breaks can support comfort.",
                    highlightMetric = "${if (hours > 0) "${hours}h " else ""}${mins}m"
                )
            )
        }

        // 4. Evening usage pattern
        if (eveningCount > 0) {
            patterns.add(
                PatternCard(
                    id = "p_evening",
                    title = "Screen activity observed in evening hours",
                    description = "Timestamps show usage past 9:00 PM. Screen light and active content can gently delay sleepiness.",
                    category = AppCategory.HEALTH_WELLNESS,
                    confidence = ConfidenceLevel.MEDIUM,
                    limitationNote = "Based on app timestamps; ScreenSense does not monitor personal bedtime.",
                    highlightMetric = "Evening activity"
                )
            )
        }

        val hours = totalDuration / (1000 * 60 * 60)
        val mins = (totalDuration / (1000 * 60)) % 60
        val formattedTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        // Build Deep AI Telemetry Report
        val realtimeReport = buildRealtimeAiReport(records, totalDuration, dominantCategory, eveningCount > 0, frequentCheckingApp, longSessionApp)

        return UsageAnalysisSummary(
            totalDurationMillis = totalDuration,
            formattedTotalTime = formattedTime,
            topApp = topApp,
            categoryBreakdown = categoryMap,
            categoryPercentages = categoryPercentages,
            totalLaunches = totalLaunches,
            patternCards = patterns,
            dominantCategory = dominantCategory,
            hasEveningUsage = eveningCount > 0,
            hasFrequentChecking = frequentCheckingApp != null,
            hasLongSession = longSessionApp != null,
            dataCompletenessText = "${records.size} apps analyzed (${records.firstOrNull()?.source ?: "SYSTEM"})",
            realtimeReport = realtimeReport
        )
    }

    private fun buildRealtimeAiReport(
        records: List<UsageRecordEntity>,
        totalDuration: Long,
        dominantCategory: AppCategory,
        hasEveningUsage: Boolean,
        frequentCheckingApp: UsageRecordEntity?,
        longSessionApp: UsageRecordEntity?
    ): RealtimeAiTelemetryReport {
        val sortedRecords = records.sortedByDescending { it.durationMillis }
        val topApps = sortedRecords.take(5)

        val appInsights = topApps.map { record ->
            val appPercent = if (totalDuration > 0) (record.durationMillis.toFloat() / totalDuration) * 100f else 0f
            val h = record.durationMillis / (1000 * 60 * 60)
            val m = (record.durationMillis / (1000 * 60)) % 60
            val durStr = if (h > 0) "${h}h ${m}m" else "${m}m"

            val (impactLevel, reason, takeaway) = generateAppSpecificAnalysis(record, durStr, appPercent)

            AppTelemetryInsight(
                packageName = record.packageName,
                appLabel = record.appLabel,
                category = record.category,
                durationMillis = record.durationMillis,
                formattedDuration = durStr,
                percentageOfTotal = appPercent,
                launchCount = record.launchCount ?: 1,
                cognitiveImpactLevel = impactLevel,
                analysisReason = reason,
                actionableTakeaway = takeaway
            )
        }

        val productiveMillis = records.filter { it.category == AppCategory.WORK_STUDY || it.category == AppCategory.HEALTH_WELLNESS }.sumOf { it.durationMillis }
        val entertainingMillis = records.filter { it.category == AppCategory.ENTERTAINMENT || it.category == AppCategory.SOCIAL_COMMUNICATION || it.category == AppCategory.BROWSING }.sumOf { it.durationMillis }
        val productivityRatio = if (totalDuration > 0) (productiveMillis.toFloat() / totalDuration) * 100f else 0f

        val totalHours = totalDuration / (1000f * 60 * 60)
        val launches = records.sumOf { it.launchCount ?: 0 }

        val loadScore = ((totalHours * 12f) + (entertainingMillis / (1000f * 60 * 60) * 15f) + (launches * 0.4f)).toInt().coerceIn(10, 95)
        val loadLabel = when {
            loadScore >= 75 -> "High Cognitive Drain"
            loadScore >= 50 -> "Elevated Screen Exposure"
            loadScore >= 30 -> "Balanced Daily Engagement"
            else -> "Optimal Low-Strain Profile"
        }

        val topAppLabel = sortedRecords.firstOrNull()?.appLabel ?: "Apps"
        val topAppDur = sortedRecords.firstOrNull()?.let {
            val h = it.durationMillis / (1000 * 60 * 60)
            val m = (it.durationMillis / (1000 * 60)) % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        } ?: ""

        val headline = if (sortedRecords.isNotEmpty()) {
            "Real-Time Telemetry: $topAppLabel led with $topAppDur (${(appInsights.firstOrNull()?.percentageOfTotal ?: 0f).toInt()}%)"
        } else {
            "Real-Time AI Telemetry Summary"
        }

        val overallSummary = buildString {
            append("Telemetry indicates ")
            if (productivityRatio > 50) {
                append("a productive work profile with ${productivityRatio.toInt()}% focused in ${dominantCategory.displayName}. ")
            } else {
                append("screen time leaning toward ${dominantCategory.displayName.lowercase()} media. ")
            }
            if (sortedRecords.size >= 2) {
                append("Primary time sinks were ${sortedRecords[0].appLabel} and ${sortedRecords[1].appLabel}. ")
            }
            if (hasEveningUsage) {
                append("Late-evening engagement detected past 9:00 PM.")
            }
        }

        val keyFindings = mutableListOf<String>()
        if (sortedRecords.isNotEmpty()) {
            val top = sortedRecords.first()
            val topH = top.durationMillis / (1000 * 60 * 60)
            val topM = (top.durationMillis / (1000 * 60)) % 60
            val topTime = if (topH > 0) "${topH}h ${topM}m" else "${topM}m"
            keyFindings.add("${top.appLabel} accounted for $topTime (${(appInsights.firstOrNull()?.percentageOfTotal ?: 0f).toInt()}% of screen time), making it today's primary focus driver.")
        }
        if (frequentCheckingApp != null) {
            keyFindings.add("${frequentCheckingApp.appLabel} was opened ${frequentCheckingApp.launchCount} times for rapid micro-sessions, creating frequent context switching.")
        }
        if (hasEveningUsage) {
            keyFindings.add("Screen light detected after 9:00 PM can delay melatonin release by up to 45 minutes.")
        }
        if (longSessionApp != null && longSessionApp != sortedRecords.firstOrNull()) {
            keyFindings.add("${longSessionApp.appLabel} held a continuous session of over 60 minutes, which benefits from physical eye and posture resets.")
        }
        if (keyFindings.size < 2) {
            keyFindings.add("Balanced category distribution observed across ${records.size} foreground applications.")
        }

        val contextSwitching = when {
            launches > 40 -> "High (Frequent Pickups)"
            launches > 20 -> "Moderate"
            else -> "Calm (Focused Sessions)"
        }

        return RealtimeAiTelemetryReport(
            headline = headline,
            overallSummary = overallSummary,
            cognitiveLoadLabel = loadLabel,
            cognitiveLoadScore = loadScore,
            topAppInsights = appInsights,
            telemetryKeyFindings = keyFindings,
            productivityRatio = productivityRatio,
            contextSwitchingFrequency = contextSwitching
        )
    }

    private fun generateAppSpecificAnalysis(
        record: UsageRecordEntity,
        formattedDuration: String,
        percentage: Float
    ): Triple<String, String, String> {
        val name = record.appLabel.lowercase()
        val pkg = record.packageName.lowercase()
        val cat = record.category
        val opens = record.launchCount ?: 1
        val pct = percentage.toInt()

        return when {
            name.contains("youtube") || name.contains("netflix") || name.contains("disney") || name.contains("twitch") || name.contains("prime video") || name.contains("hulu") -> {
                Triple(
                    "High Visual Drain & Dopamine Stacking",
                    "${record.appLabel} was active for $formattedDuration ($pct% of total screen time). Extended continuous streaming fixes optical focal distance at ~40cm, drops blink rate by over 60%, and delivers steady dopamine hits that elevate subsequent task resistance.",
                    "Engage the 20-20-20 rule: look 20 feet away for 20 seconds, or switch to background audio mode."
                )
            }
            name.contains("tiktok") || name.contains("reels") || name.contains("shorts") -> {
                Triple(
                    "Hyper-Fast Dopamine Loops",
                    "${record.appLabel} logged $formattedDuration across $opens launch cycles. Ultra-short algorithmic video clips induce rapid context resets that erode sustained working memory and induce restlessness.",
                    "Set an explicit 10-minute exploration boundary before launching ${record.appLabel} again."
                )
            }
            name.contains("instagram") || name.contains("facebook") || name.contains("threads") || name.contains("snapchat") -> {
                Triple(
                    "Social Comparison & Feed Friction",
                    "${record.appLabel} was opened $opens times today for $formattedDuration ($pct% of daily time). Variable reward feeds stimulate intermittent social validation loops that fragment concentration.",
                    "Batch ${record.appLabel} check-ins to twice daily and disable non-essential notification badges."
                )
            }
            name.contains("twitter") || name.contains(" x") || name.contains("reddit") || pkg.contains("reddit") -> {
                Triple(
                    "Cognitive Over-Stimulation",
                    "${record.appLabel} accounted for $formattedDuration across $opens sessions. High-density information threads trigger rapid analytical appraisal, elevating cognitive load and emotional tension.",
                    "Pause reading after 15 minutes and step away from the device for a 3-minute physical reset."
                )
            }
            name.contains("whatsapp") || name.contains("telegram") || name.contains("discord") || name.contains("messages") || name.contains("signal") -> {
                Triple(
                    "Continuous Communication Latency",
                    "${record.appLabel} recorded $opens opens totaling $formattedDuration. Constant messaging availability creates an 'ambient vigilance' state where the brain anticipates incoming pings.",
                    "Establish a 45-minute focused offline block by placing your phone on Do Not Disturb."
                )
            }
            name.contains("slack") || name.contains("teams") || name.contains("zoom") || name.contains("meet") -> {
                Triple(
                    "Workplace Hyper-Responsiveness",
                    "${record.appLabel} logged $formattedDuration of work communication today. Rapid conversational responsiveness at work interrupts deep architectural problem-solving.",
                    "Block out scheduled calendar focus hours to minimize reactive message checking in ${record.appLabel}."
                )
            }
            name.contains("gmail") || name.contains("outlook") || name.contains("mail") || name.contains("inbox") -> {
                Triple(
                    "Inbox Processing Load",
                    "${record.appLabel} was checked $opens times totaling $formattedDuration. Asynchronous triage creates micro-decision fatigue from repeatedly evaluating message priorities.",
                    "Batch email processing into two designated 20-minute daily triage windows."
                )
            }
            name.contains("notion") || name.contains("docs") || name.contains("sheets") || name.contains("word") || name.contains("excel") || name.contains("notes") || name.contains("studio") || name.contains("code") -> {
                Triple(
                    "Deep Analytical Work Session",
                    "${record.appLabel} represented $formattedDuration of concentrated output. While highly productive, continuous screen-bound synthesis strains spinal posture and depletes executive function.",
                    "Practice the 50/10 rhythm: 50 minutes of deep creation in ${record.appLabel} followed by 10 minutes of movement and water."
                )
            }
            name.contains("spotify") || name.contains("music") || name.contains("podcast") || name.contains("audible") -> {
                Triple(
                    "Auditory Background Engagement",
                    "${record.appLabel} was active for $formattedDuration. Audio media is gentle on visual faculties, though continuous speech audio can subtly compete with verbal working memory.",
                    "Enjoy audio accompaniment while maintaining screen-free walking or household tasks."
                )
            }
            name.contains("chrome") || name.contains("firefox") || name.contains("browser") || name.contains("safari") || name.contains("edge") -> {
                Triple(
                    "Non-Linear Information Foraging",
                    "${record.appLabel} was active for $formattedDuration ($pct% of total). Exploring divergent browser tabs can lead to unplanned rabbit holes and mental fatigue.",
                    "Bookmark interesting articles into a dedicated reading list for a single evening session."
                )
            }
            name.contains("game") || name.contains("clash") || name.contains("roblox") || name.contains("genshin") || name.contains("candy") || name.contains("chess") -> {
                Triple(
                    "Intense Interactive Dopamine Loop",
                    "${record.appLabel} held your attention for $formattedDuration. Fast-paced interactive gaming delivers intense sensory feedback and adrenaline that delays natural relaxation.",
                    "Set a physical timer for gaming sessions and follow with 2 minutes of calming box breathing."
                )
            }
            name.contains("amazon") || name.contains("ebay") || name.contains("shopping") || name.contains("shopee") || name.contains("aliexpress") -> {
                Triple(
                    "Exploratory Decision Fatigue",
                    "${record.appLabel} logged $formattedDuration across $opens sessions. Comparing products and deals exerts significant comparative decision fatigue.",
                    "Place desired items into a wish list and sleep on purchase decisions before checking out."
                )
            }
            name.contains("maps") || name.contains("waze") || name.contains("uber") || name.contains("lyft") -> {
                Triple(
                    "Active Navigation & Utility",
                    "${record.appLabel} was utilized for $formattedDuration for navigation or transit. Real-world mobility utility with minimal background cognitive drain.",
                    "Rest eyes once arriving safely at your destination."
                )
            }
            cat == AppCategory.ENTERTAINMENT -> {
                Triple(
                    "Passive Leisure Engagement",
                    "${record.appLabel} consumed $formattedDuration ($pct% of today). Extended entertainment media relaxes the body but can prolong sedentary posture.",
                    "Pair ${record.appLabel} entertainment time with a brief standing stretch or water refill."
                )
            }
            cat == AppCategory.WORK_STUDY -> {
                Triple(
                    "Focused Task Execution",
                    "${record.appLabel} logged $formattedDuration in productive workflow. Sustained problem solving depletes glucose reserves in the prefrontal cortex.",
                    "Take a 3-minute physical reset away from your screen to restore cognitive stamina."
                )
            }
            cat == AppCategory.SOCIAL_COMMUNICATION -> {
                Triple(
                    "Social Connectivity Load",
                    "${record.appLabel} was accessed $opens times totaling $formattedDuration. Social engagement fosters connection but requires mindful boundaries to avoid digital overstimulation.",
                    "Set an intentional duration before entering ${record.appLabel} conversations."
                )
            }
            cat == AppCategory.BROWSING -> {
                Triple(
                    "Web Exploration & Reading",
                    "${record.appLabel} accounted for $formattedDuration ($pct% of total). Multi-topic reading expands knowledge but can create slight mental fog when uninterrupted.",
                    "Close unused background tabs and rest eyes for 60 seconds."
                )
            }
            else -> {
                val avgMins = if (opens > 0) (record.durationMillis / (1000 * 60 * opens)).toInt() else 1
                Triple(
                    "Targeted App Interaction",
                    "${record.appLabel} logged $formattedDuration across $opens check-in${if (opens > 1) "s" else ""} (averaging ~${avgMins}m per session, $pct% of screen time).",
                    "Keep ${record.appLabel} sessions deliberate and transition smoothly to your next priority."
                )
            }
        }
    }
}

