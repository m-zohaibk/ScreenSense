package com.example.domain

import com.example.data.model.ActionEffort
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import com.example.data.model.WellnessAction
import com.example.data.model.WellnessGoal

data class FocusChoice(
    val id: String,
    val categoryTag: String,
    val title: String,
    val rationale: String,
    val timeEstimate: String,
    val goal: WellnessGoal,
    val action: WellnessAction
)

data class AiBestSuggestion(
    val id: String,
    val badge: String,
    val targetAppName: String,
    val title: String,
    val shortAction: String,
    val validReason: String,
    val expectedBenefit: String,
    val estimatedMinutes: Int,
    val action: WellnessAction
)

object RecommendationEngine {

    fun generateRecommendation(
        goal: WellnessGoal,
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        return when (goal) {
            WellnessGoal.SLEEP -> generateSleepRecommendation(analysis, todayDate)
            WellnessGoal.FOCUS -> generateFocusRecommendation(analysis, todayDate)
            WellnessGoal.LESS_STRESS -> generateStressRecommendation(analysis, todayDate)
            WellnessGoal.MOVEMENT -> generateMovementRecommendation(analysis, todayDate)
            WellnessGoal.OFFLINE_CONNECTION -> generateOfflineRecommendation(analysis, todayDate)
            WellnessGoal.DIGITAL_BALANCE -> generateBalanceRecommendation(analysis, todayDate)
        }
    }

    fun generateAiBestSuggestions(
        goal: WellnessGoal,
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): List<AiBestSuggestion> {
        val topApp = analysis.topApp
        val topInsight = analysis.realtimeReport.topAppInsights.firstOrNull()
        val suggestions = mutableListOf<AiBestSuggestion>()

        if (topApp != null && topInsight != null) {
            val appName = topApp.appLabel
            val dur = topInsight.formattedDuration
            val pct = topInsight.percentageOfTotal.toInt()
            val nameLower = appName.lowercase()

            when {
                nameLower.contains("youtube") || nameLower.contains("netflix") || nameLower.contains("video") || nameLower.contains("twitch") || nameLower.contains("disney") || topApp.category == AppCategory.ENTERTAINMENT -> {
                    val act = WellnessAction(
                        id = "ai_rec_video_$todayDate",
                        title = "5-Minute Eye & Focus Break from $appName",
                        description = "Pause $appName and look away from all glowing screens. Focus on an object at least 20 feet away or step outdoors for 5 minutes.",
                        goal = WellnessGoal.FOCUS,
                        effortLevel = ActionEffort.TINY,
                        reason = "$appName consumed $dur ($pct% of total screen time today). Extended continuous video streaming causes ciliary muscle spasm and delivers continuous dopamine stimulation that reduces subsequent focus stamina.",
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Blink softly several times to restore natural ocular tear film.",
                        alternatives = listOf("Switch to audio podcast mode", "Gently palm your eyes with warm hands", "Take a hydration walk"),
                        status = "PENDING",
                        date = todayDate
                    )
                    suggestions.add(
                        AiBestSuggestion(
                            id = "best_sug_1",
                            badge = "🌟 Top AI Recommendation",
                            targetAppName = appName,
                            title = "5-Minute Visual Reset from $appName",
                            shortAction = "Look 20ft away or step away from screen",
                            validReason = "$appName accounted for $dur ($pct% of today's screen time). After 45+ minutes of continuous video stream, ocular fatigue peaks and working memory responsiveness declines by 30%.",
                            expectedBenefit = "Relieves eye strain, resets dopamine baseline, and restores mental clarity.",
                            estimatedMinutes = 5,
                            action = act
                        )
                    )
                }

                nameLower.contains("instagram") || nameLower.contains("tiktok") || nameLower.contains("facebook") || nameLower.contains("twitter") || nameLower.contains(" x") || nameLower.contains("reddit") || nameLower.contains("snapchat") || topApp.category == AppCategory.SOCIAL_COMMUNICATION -> {
                    val opens = topApp.launchCount ?: 1
                    val act = WellnessAction(
                        id = "ai_rec_social_$todayDate",
                        title = "Set a Mindful 15-Minute Boundary for $appName",
                        description = "Pause before opening $appName again. Decide on an intentional viewing time or place your phone out of reach for the next hour.",
                        goal = WellnessGoal.DIGITAL_BALANCE,
                        effortLevel = ActionEffort.TINY,
                        reason = "$appName was opened $opens times totaling $dur. Algorithmic variable rewards trigger frequent context switching that fragments deep focus.",
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Awareness without guilt is the key to healthy digital boundaries.",
                        alternatives = listOf("Mute non-urgent notifications", "Move $appName into a secondary folder", "Step away for a quick water break"),
                        status = "PENDING",
                        date = todayDate
                    )
                    suggestions.add(
                        AiBestSuggestion(
                            id = "best_sug_1",
                            badge = "🌟 Top AI Recommendation",
                            targetAppName = appName,
                            title = "Mindful Boundary for $appName",
                            shortAction = "Set an intentional check-in window",
                            validReason = "$appName had $opens separate launches today. Rapid checking cycles interrupt deep thought and sustain background mental restlessness.",
                            expectedBenefit = "Reclaims attention bandwidth and prevents unconscious scrolling loops.",
                            estimatedMinutes = 2,
                            action = act
                        )
                    )
                }

                nameLower.contains("whatsapp") || nameLower.contains("telegram") || nameLower.contains("discord") || nameLower.contains("messages") || nameLower.contains("signal") -> {
                    val opens = topApp.launchCount ?: 1
                    val act = WellnessAction(
                        id = "ai_rec_messaging_$todayDate",
                        title = "Create a 30-Minute Quiet Space from $appName",
                        description = "Mute active chat notifications and dedicate the next 30 minutes to unbroken real-world focus.",
                        goal = WellnessGoal.FOCUS,
                        effortLevel = ActionEffort.TINY,
                        reason = "$appName recorded $opens check-ins totaling $dur. Continuous ambient availability fragments working memory through unpredictable ping alerts.",
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Keep critical contact bypass enabled if necessary.",
                        alternatives = listOf("Batch message replies at the top of the hour", "Switch phone to Do Not Disturb"),
                        status = "PENDING",
                        date = todayDate
                    )
                    suggestions.add(
                        AiBestSuggestion(
                            id = "best_sug_1",
                            badge = "🌟 Top AI Recommendation",
                            targetAppName = appName,
                            title = "30-Minute Quiet Space from $appName",
                            shortAction = "Pause incoming chat notifications",
                            validReason = "$appName was opened $opens times today ($dur total). Constant communication readiness keeps your cognitive radar on alert.",
                            expectedBenefit = "Eliminates attentional drag and fosters deep mental peace.",
                            estimatedMinutes = 2,
                            action = act
                        )
                    )
                }

                nameLower.contains("slack") || nameLower.contains("teams") || nameLower.contains("zoom") || nameLower.contains("studio") || nameLower.contains("code") || nameLower.contains("notion") || topApp.category == AppCategory.WORK_STUDY -> {
                    val act = WellnessAction(
                        id = "ai_rec_work_$todayDate",
                        title = "2-Minute Posture & Hydration Break after $appName",
                        description = "Stand up, roll your shoulders backward 5 times, and drink a glass of water away from your desk.",
                        goal = WellnessGoal.MOVEMENT,
                        effortLevel = ActionEffort.TINY,
                        reason = "$appName logged $dur of continuous work focus. Prolonged desk posture tightens cervical spine muscles and reduces oxygen circulation.",
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Move naturally and avoid forcing any neck stretches.",
                        alternatives = listOf("3 seated spinal twists", "10 calf raises", "60-second deep belly breathing"),
                        status = "PENDING",
                        date = todayDate
                    )
                    suggestions.add(
                        AiBestSuggestion(
                            id = "best_sug_1",
                            badge = "🌟 Top AI Recommendation",
                            targetAppName = appName,
                            title = "2-Minute Posture Reset for $appName",
                            shortAction = "Stand, stretch shoulders, and hydrate",
                            validReason = "You've logged $dur in $appName. Sustained cognitive focus without physical reset leads to physical fatigue and late-afternoon burnout.",
                            expectedBenefit = "Re-energizes circulation, eases neck tension, and sharpens analytical thinking.",
                            estimatedMinutes = 2,
                            action = act
                        )
                    )
                }

                nameLower.contains("chrome") || nameLower.contains("firefox") || nameLower.contains("browser") || nameLower.contains("safari") || nameLower.contains("edge") || topApp.category == AppCategory.BROWSING -> {
                    val act = WellnessAction(
                        id = "ai_rec_browser_$todayDate",
                        title = "Tab Consolidation & Eye Reset for $appName",
                        description = "Close or bookmark open background tabs in $appName and close your eyes for 60 seconds.",
                        goal = WellnessGoal.DIGITAL_BALANCE,
                        effortLevel = ActionEffort.TINY,
                        reason = "$appName accounted for $dur ($pct% of screen time). Non-linear tab reading increases cognitive load and causes ocular fatigue.",
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Save valuable reading materials for a single planned session.",
                        alternatives = listOf("Save links to a reading list", "Do 3 slow deep breaths"),
                        status = "PENDING",
                        date = todayDate
                    )
                    suggestions.add(
                        AiBestSuggestion(
                            id = "best_sug_1",
                            badge = "🌟 Top AI Recommendation",
                            targetAppName = appName,
                            title = "Tab Consolidation for $appName",
                            shortAction = "Close background tabs & rest eyes",
                            validReason = "$appName accumulated $dur across multi-tab browsing. Rapidly switching between open web pages fragments working memory.",
                            expectedBenefit = "Clears mental clutter and prevents browsing rabbit holes.",
                            estimatedMinutes = 2,
                            action = act
                        )
                    )
                }

                else -> {
                    val act = WellnessAction(
                        id = "ai_rec_custom_${appName.hashCode()}_$todayDate",
                        title = "Targeted Focus Pause from $appName",
                        description = "Take a conscious 2-minute transition break away from $appName before starting your next task.",
                        goal = goal,
                        effortLevel = ActionEffort.TINY,
                        reason = "$appName represented $dur ($pct% of today's screen time). Taking a targeted micro-pause directly reinforces your ${goal.displayName} goal.",
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Small micro-pauses prevent cumulative fatigue across the day.",
                        alternatives = listOf("Hydrate with a glass of water", "60-second shoulder roll and breath"),
                        status = "PENDING",
                        date = todayDate
                    )
                    suggestions.add(
                        AiBestSuggestion(
                            id = "best_sug_1",
                            badge = "🌟 Top AI Recommendation",
                            targetAppName = appName,
                            title = "Mindful Transition from $appName",
                            shortAction = "Take a 2-minute pause away from screen",
                            validReason = "$appName was your primary app today with $dur ($pct% of total). Pausing between app sessions builds healthy digital boundaries.",
                            expectedBenefit = "Restores cognitive bandwidth and supports ${goal.displayName}.",
                            estimatedMinutes = 2,
                            action = act
                        )
                    )
                }
            }
        } else {
            val act = generateRecommendation(goal, analysis, todayDate)
            suggestions.add(
                AiBestSuggestion(
                    id = "best_sug_1",
                    badge = "🌟 Top AI Recommendation",
                    targetAppName = "All Apps",
                    title = act.title,
                    shortAction = act.description,
                    validReason = "Based on your goal for ${goal.displayName}. A small intentional step creates sustainable daily wellness.",
                    expectedBenefit = "Improves mindful presence and daily balance.",
                    estimatedMinutes = 2,
                    action = act
                )
            )
        }

        // Add 2nd suggestion: Evening or Micro-Reset
        if (analysis.hasEveningUsage) {
            val eveningAction = WellnessAction(
                id = "ai_rec_evening_$todayDate",
                title = "15-Minute Evening Screen Wind-Down",
                description = "Dim ambient lights and switch devices to nighttime warm display mode 30 minutes before sleep.",
                goal = WellnessGoal.SLEEP,
                effortLevel = ActionEffort.TINY,
                reason = "Late screen activity detected past 9:00 PM. Blue spectrum photons suppress natural melatonin secretion by up to 50%.",
                confidence = ConfidenceLevel.HIGH,
                safetyNote = "Keep emergency contacts unmuted if on-call.",
                alternatives = listOf("Switch to paper book or warm audio", "Gentle 3-minute evening gratitude log"),
                status = "PENDING",
                date = todayDate
            )
            suggestions.add(
                AiBestSuggestion(
                    id = "best_sug_evening",
                    badge = "🌙 Sleep & Wind-Down",
                    targetAppName = "Late-Night Usage",
                    title = "15-Minute Evening Wind-Down",
                    shortAction = "Dim screen & switch to warm buffer",
                    validReason = "Evening screen timestamps past 9:00 PM delay your circadian melatonin peak by up to 45 minutes.",
                    expectedBenefit = "Shortens sleep latency and improves deep sleep quality.",
                    estimatedMinutes = 15,
                    action = eveningAction
                )
            )
        } else {
            val microAction = WellnessAction(
                id = "ai_rec_micro_$todayDate",
                title = "60-Second Box Breathing Reset",
                description = "Breathe in for 4 seconds, hold for 4, exhale for 4, and hold for 4. Repeat 3 cycles.",
                goal = WellnessGoal.LESS_STRESS,
                effortLevel = ActionEffort.TINY,
                reason = "Screen time involves shallow 'screen apnea' breathing. Box breathing activates the vagus nerve and lowers resting heart rate.",
                confidence = ConfidenceLevel.HIGH,
                safetyNote = "Breathe comfortably without straining.",
                alternatives = listOf("Gentle neck rolls", "Close eyes and soften jaw"),
                status = "PENDING",
                date = todayDate
            )
            suggestions.add(
                AiBestSuggestion(
                    id = "best_sug_micro",
                    badge = "⚡ Quick Cognitive Recharge",
                    targetAppName = "Stress Reset",
                    title = "60-Second Box Breathing",
                    shortAction = "4-4-4-4 breathing cycle away from screen",
                    validReason = "Intense screen engagement often causes shallow breathing. 60 seconds of paced breath down-regulates nervous system tension.",
                    expectedBenefit = "Lowers cortisol, reduces eye strain, and clears mental fog.",
                    estimatedMinutes = 1,
                    action = microAction
                )
            )
        }

        return suggestions
    }

    fun generateMultiChoiceRecommendations(
        goal: WellnessGoal,
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): List<FocusChoice> {
        val primaryAction = generateRecommendation(goal, analysis, todayDate)

        val topAppName = analysis.topApp?.appLabel ?: "top apps"
        val microPauseAction = WellnessAction(
            id = "choice_micro_pause_$todayDate",
            title = "60-Second Eye & Screen Pause",
            description = "Look at an object at least 20 feet away or close your eyes gently for 60 seconds to relax your optic nerves.",
            goal = WellnessGoal.FOCUS,
            effortLevel = ActionEffort.TINY,
            reason = "With ${analysis.formattedTotalTime} active screen time today (led by $topAppName), a brief micro-pause restores visual and cognitive clarity.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Blink softly several times to replenish natural tear film.",
            alternatives = listOf("Gentle eye palming with warm hands", "Look out a nearby window"),
            status = "PENDING",
            date = todayDate
        )

        val physicalResetAction = WellnessAction(
            id = "choice_physical_reset_$todayDate",
            title = "Hydrate & Posture Reset",
            description = "Stand up, roll your shoulders backward 5 times, and drink a glass of water away from all devices.",
            goal = WellnessGoal.MOVEMENT,
            effortLevel = ActionEffort.TINY,
            reason = if (analysis.hasLongSession) "A sustained session on $topAppName was observed. Standing up recharges blood circulation." else "Regular posture check-ins prevent spinal and shoulder tension.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Move naturally and avoid forcing any stretches.",
            alternatives = listOf("3 seated torso twists", "Quick wrist and forearm stretch"),
            status = "PENDING",
            date = todayDate
        )

        val appBoundaryAction = WellnessAction(
            id = "choice_app_boundary_$todayDate",
            title = "Intentional App Boundary for $topAppName",
            description = "Pause before opening $topAppName again. Decide your exact objective or set a polite 15-minute timer before exploring.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            effortLevel = ActionEffort.TINY,
            reason = "${analysis.dominantCategory.displayName} ($topAppName) was your highest category today. Conscious intent stops mindless rabbit holes.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Your phone is a tool for your life, not a master of your time.",
            alternatives = listOf("Mute non-essential notifications for 30m", "Move $topAppName into a folder"),
            status = "PENDING",
            date = todayDate
        )

        return listOf(
            FocusChoice(
                id = "choice_1_$todayDate",
                categoryTag = "Micro-Pause",
                title = microPauseAction.title,
                rationale = microPauseAction.reason,
                timeEstimate = "1 min",
                goal = WellnessGoal.FOCUS,
                action = microPauseAction
            ),
            FocusChoice(
                id = "choice_2_$todayDate",
                categoryTag = "Physical Reset",
                title = physicalResetAction.title,
                rationale = physicalResetAction.reason,
                timeEstimate = "3 min",
                goal = WellnessGoal.MOVEMENT,
                action = physicalResetAction
            ),
            FocusChoice(
                id = "choice_3_$todayDate",
                categoryTag = "App Boundary",
                title = appBoundaryAction.title,
                rationale = appBoundaryAction.reason,
                timeEstimate = "5 min",
                goal = WellnessGoal.DIGITAL_BALANCE,
                action = appBoundaryAction
            )
        )
    }

    private fun generateSleepRecommendation(
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        val topAppName = analysis.topApp?.appLabel ?: "devices"
        return if (analysis.hasEveningUsage) {
            WellnessAction(
                id = "rec_sleep_winddown_$todayDate",
                title = "Try a 15-minute screen-free wind-down from $topAppName",
                description = "Give your eyes and mind a calm buffer before resting. Try switching to a physical book, gentle stretch, or quiet music.",
                goal = WellnessGoal.SLEEP,
                effortLevel = ActionEffort.MEDIUM,
                reason = "Usage timestamps on $topAppName show screen activity into evening hours. A gradual transition helps natural melatonin production.",
                confidence = ConfidenceLevel.HIGH,
                safetyNote = "Choose comfortable lighting; there is no need for total darkness if you are moving around.",
                alternatives = listOf(
                    "Dim screen brightness and enable nighttime warm display filter",
                    "Listen to a relaxing audio track instead of watching video",
                    "A quick 3-minute evening gratitude jot down"
                ),
                status = "PENDING",
                date = todayDate
            )
        } else {
            WellnessAction(
                id = "rec_sleep_prepare_$todayDate",
                title = "Set a peaceful bedtime intention",
                description = "Pick a target wind-down time tonight and place your phone a comfortable arm's length away from your pillow.",
                goal = WellnessGoal.SLEEP,
                effortLevel = ActionEffort.TINY,
                reason = "Based on your goal for a restful sleep routine. Small environmental cues support consistent rest.",
                confidence = ConfidenceLevel.MEDIUM,
                safetyNote = "Keep essential emergency ringtones on if you are on call or caring for family.",
                alternatives = listOf(
                    "Switch off non-urgent group notifications for the night",
                    "Sip a warm caffeine-free herbal tea or water",
                    "Do 2 minutes of gentle shoulder breathing"
                ),
                status = "PENDING",
                date = todayDate
            )
        }
    }

    private fun generateFocusRecommendation(
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        val topAppName = analysis.topApp?.appLabel ?: "top apps"
        return if (analysis.hasFrequentChecking) {
            WellnessAction(
                id = "rec_focus_single_task_$todayDate",
                title = "Single-task window: batch check-ins on $topAppName",
                description = "Try grouping check-ins on $topAppName into set moments rather than frequent unlocks throughout the hour.",
                goal = WellnessGoal.FOCUS,
                effortLevel = ActionEffort.TINY,
                reason = "You opened $topAppName multiple times for short intervals. Batching check-ins reduces cognitive friction and reclaims working memory.",
                confidence = ConfidenceLevel.HIGH,
                safetyNote = "Set an alarm if you need to be reachable for a specific time-sensitive matter.",
                alternatives = listOf(
                    "Turn your phone face down for 25 minutes of unbroken focus",
                    "Turn on Do Not Disturb for 30 minutes",
                    "Take 3 deep breaths before opening the next app"
                ),
                status = "PENDING",
                date = todayDate
            )
        } else {
            WellnessAction(
                id = "rec_focus_intentional_$todayDate",
                title = "Define your intention before unlocking",
                description = "Take one conscious breath and state your goal before opening your next app session.",
                goal = WellnessGoal.FOCUS,
                effortLevel = ActionEffort.TINY,
                reason = "Supports purposeful device use and prevents incidental mindless scrolling.",
                confidence = ConfidenceLevel.MEDIUM,
                safetyNote = "Take your time; small habits compound over weeks.",
                alternatives = listOf(
                    "Keep your home screen minimal with only essential tools",
                    "Put distracting apps into a named 'Later' folder",
                    "Pause for 5 seconds when reaching for your phone"
                ),
                status = "PENDING",
                date = todayDate
            )
        }
    }

    private fun generateStressRecommendation(
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        val topAppName = analysis.topApp?.appLabel ?: "screen"
        return WellnessAction(
            id = "rec_stress_microbreak_$todayDate",
            title = "Take a 60-second nervous system reset from $topAppName",
            description = "Unclench your jaw, drop your shoulders, and take three slow exhales away from glowing displays.",
            goal = WellnessGoal.LESS_STRESS,
            effortLevel = ActionEffort.TINY,
            reason = "Continuous attention on $topAppName can cause subconscious screen apnea. Brief somatic resets lower resting cortisol.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Listen to your body; if sitting feels tense, gently stand or walk a few steps.",
            alternatives = listOf(
                "Gently look out a window at a distant horizon",
                "Drink a cool glass of water with no device in hand",
                "Listen to a 1-minute ambient sound or silence"
            ),
            status = "PENDING",
            date = todayDate
        )
    }

    private fun generateMovementRecommendation(
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        val topAppName = analysis.topApp?.appLabel ?: "apps"
        return WellnessAction(
            id = "rec_movement_stand_$todayDate",
            title = "Stand and stretch after $topAppName session",
            description = "Spend 2 minutes walking, rolling your ankles, or reaching your arms upward away from the desk.",
            goal = WellnessGoal.MOVEMENT,
            effortLevel = ActionEffort.TINY,
            reason = "${analysis.formattedTotalTime} total screen time today (led by $topAppName). Regular movement breaks keep circulation active and relieve spinal load.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Move gently and stay within your comfortable range of motion.",
            alternatives = listOf(
                "Do 10 seated calf raises while reviewing your work",
                "Take a brisk 3-minute stroll around your room or hallway",
                "Gentle wrist and finger tendon glides"
            ),
            status = "PENDING",
            date = todayDate
        )
    }

    private fun generateOfflineRecommendation(
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        return WellnessAction(
            id = "rec_offline_face_$todayDate",
            title = "Create a device-free conversation moment",
            description = "During your next meal or conversation, keep your phone tucked away or in a pocket to be fully present.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            effortLevel = ActionEffort.TINY,
            reason = "Removing visible devices from shared spaces significantly increases conversational depth and emotional rapport.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Let loved ones know you are taking a brief disconnected break if helpful.",
            alternatives = listOf(
                "Call a friend or family member for a short voice check-in",
                "Write a physical thank-you note or postcard",
                "Enjoy an afternoon tea or snack with zero screens active"
            ),
            status = "PENDING",
            date = todayDate
        )
    }

    private fun generateBalanceRecommendation(
        analysis: UsageAnalysisSummary,
        todayDate: String
    ): WellnessAction {
        val topAppName = analysis.topApp?.appLabel ?: "top apps"
        return WellnessAction(
            id = "rec_balance_boundary_$todayDate",
            title = "Acknowledge today's balance on $topAppName",
            description = "Celebrate what you accomplished today and set a gentle boundary for your final device check.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            effortLevel = ActionEffort.TINY,
            reason = "With ${analysis.formattedTotalTime} recorded today, conscious closing rituals help prevent open-ended nighttime browsing.",
            confidence = ConfidenceLevel.HIGH,
            safetyNote = "Every small step toward balance counts; progress is not all-or-nothing.",
            alternatives = listOf(
                "Review today's small wins before signing off",
                "Pick one app to pause notifications on until morning",
                "Charge your phone across the room overnight"
            ),
            status = "PENDING",
            date = todayDate
        )
    }
}
