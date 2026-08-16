package com.example.data.model

import kotlin.random.Random

object MotivationQuotesData {
    val quotes: List<MotivationQuote> = listOf(
        // Sleep & Rest
        MotivationQuote(
            id = "q_rest_1",
            quote = "Rest is part of caring for yourself, not a reward for finishing everything.",
            goal = WellnessGoal.SLEEP,
            theme = "Rest & Recovery",
            author = "Circadian Science"
        ),
        MotivationQuote(
            id = "q_rest_2",
            quote = "The night does not require you to solve tomorrow's problems.",
            goal = WellnessGoal.SLEEP,
            theme = "Night Peace",
            author = "Evening Reflection"
        ),
        MotivationQuote(
            id = "q_rest_3",
            quote = "Dimming screen light before bed protects your natural melatonin rhythms and sleep architecture.",
            goal = WellnessGoal.SLEEP,
            theme = "Circadian Health",
            author = "Neurobiology Insights"
        ),
        MotivationQuote(
            id = "q_rest_4",
            quote = "Closing your applications is a gentle boundary that tells your nervous system it is safe to unwind.",
            goal = WellnessGoal.SLEEP,
            theme = "Sleep Boundary",
            author = "Mindful Evening"
        ),
        MotivationQuote(
            id = "q_rest_5",
            quote = "Sleep is the brain's natural rinse cycle. Honor the quiet hours.",
            goal = WellnessGoal.SLEEP,
            theme = "Brain Restoration",
            author = "Cognitive Wellness"
        ),
        MotivationQuote(
            id = "q_rest_6",
            quote = "A peaceful morning is born the night before when screens gently go dark.",
            goal = WellnessGoal.SLEEP,
            theme = "Morning Energy",
            author = "Daily Rhythm"
        ),

        // Focus & Deep Work
        MotivationQuote(
            id = "q_focus_1",
            quote = "One intentional choice is more useful than a perfect day.",
            goal = WellnessGoal.FOCUS,
            theme = "Clarity",
            author = "Intentional Living"
        ),
        MotivationQuote(
            id = "q_focus_2",
            quote = "Attention is a finite energy. Spend it on what genuinely matters to you.",
            goal = WellnessGoal.FOCUS,
            theme = "Attention",
            author = "Cognitive Psychology"
        ),
        MotivationQuote(
            id = "q_focus_3",
            quote = "Single-tasking allows your brain to enter a state of effortless flow.",
            goal = WellnessGoal.FOCUS,
            theme = "Deep Flow",
            author = "Attention Science"
        ),
        MotivationQuote(
            id = "q_focus_4",
            quote = "When you resist the impulse to check a notification, you strengthen your focus muscle.",
            goal = WellnessGoal.FOCUS,
            theme = "Focus Discipline",
            author = "Neuroplasticity Guide"
        ),
        MotivationQuote(
            id = "q_focus_5",
            quote = "Depth of thought requires silence between sensory inputs.",
            goal = WellnessGoal.FOCUS,
            theme = "Mental Quiet",
            author = "Deep Work Practice"
        ),
        MotivationQuote(
            id = "q_focus_6",
            quote = "Clarity is not about doing more; it is about protecting the space for what is essential.",
            goal = WellnessGoal.FOCUS,
            theme = "Essentialism",
            author = "Mindful Focus"
        ),

        // Digital Balance & Autonomy
        MotivationQuote(
            id = "q_balance_1",
            quote = "Your screen is a tool. You are allowed to choose how it fits into your life.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Balance",
            author = "Digital Autonomy"
        ),
        MotivationQuote(
            id = "q_balance_2",
            quote = "Digital wellness is not about deprivation; it is about conscious presence.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Intentionality",
            author = "Digital Ecology"
        ),
        MotivationQuote(
            id = "q_balance_3",
            quote = "You do not need to consume every piece of information that crosses your feed.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Information Diet",
            author = "Mindful Consumption"
        ),
        MotivationQuote(
            id = "q_balance_4",
            quote = "Reclaiming ten minutes of your screen time is reclaiming ten minutes of your life.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Time Autonomy",
            author = "Presence Guide"
        ),
        MotivationQuote(
            id = "q_balance_5",
            quote = "Algorithms are engineered to capture attention, but awareness gives you the choice to disengage.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Conscious Choice",
            author = "Humane Technology"
        ),
        MotivationQuote(
            id = "q_balance_6",
            quote = "The greatest luxury in the digital age is an uninterrupted hour with your own thoughts.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Solitude",
            author = "Quiet Minds"
        ),

        // Stress Reduction & Self-Compassion
        MotivationQuote(
            id = "q_recovery_1",
            quote = "A pause is information, not failure.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Self-Compassion",
            author = "Mindful Health"
        ),
        MotivationQuote(
            id = "q_recovery_2",
            quote = "Give yourself permission to slow down without needing an excuse.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Calm",
            author = "Nervous System Health"
        ),
        MotivationQuote(
            id = "q_recovery_3",
            quote = "Taking three deep breaths activates your parasympathetic nervous system and dissolves urgency.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Physiological Reset",
            author = "Breathwork Science"
        ),
        MotivationQuote(
            id = "q_recovery_4",
            quote = "You are not obligated to be instantly reachable to the entire world at all times.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Boundaries",
            author = "Peace of Mind"
        ),
        MotivationQuote(
            id = "q_recovery_5",
            quote = "Notice the physical sensation of holding tension, and exhale it softly into the room.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Somatic Release",
            author = "Body Awareness"
        ),
        MotivationQuote(
            id = "q_recovery_6",
            quote = "Progress in digital wellness is gentle and non-linear. Celebrate every small moment of presence.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Gentle Growth",
            author = "Compassionate Habits"
        ),

        // Movement & Physical Ease
        MotivationQuote(
            id = "q_movement_1",
            quote = "A small change in position can be a kind change to your body.",
            goal = WellnessGoal.MOVEMENT,
            theme = "Physical Ease",
            author = "Ergonomic Wisdom"
        ),
        MotivationQuote(
            id = "q_movement_2",
            quote = "Movement invites fresh energy. Even a single shoulder roll resets posture.",
            goal = WellnessGoal.MOVEMENT,
            theme = "Vitality",
            author = "Posture Science"
        ),
        MotivationQuote(
            id = "q_movement_3",
            quote = "Your eyes and neck were designed to look at distant horizons, not only glowing rectangles.",
            goal = WellnessGoal.MOVEMENT,
            theme = "20-20-20 Vision",
            author = "Visual Health"
        ),
        MotivationQuote(
            id = "q_movement_4",
            quote = "A short two-minute stroll increases cerebral blood flow and clears mental fog.",
            goal = WellnessGoal.MOVEMENT,
            theme = "Micro-Walks",
            author = "Movement Research"
        ),
        MotivationQuote(
            id = "q_movement_5",
            quote = "Honor your spine with tall posture and your lungs with expansive breaths.",
            goal = WellnessGoal.MOVEMENT,
            theme = "Alignment",
            author = "Somatic Ergonomics"
        ),
        MotivationQuote(
            id = "q_movement_6",
            quote = "Physical movement is the fastest bridge from overthinking back into your body.",
            goal = WellnessGoal.MOVEMENT,
            theme = "Grounded Presence",
            author = "Embodied Living"
        ),

        // Offline Connection & Social Presence
        MotivationQuote(
            id = "q_connect_1",
            quote = "Support can begin with one honest, low-pressure conversation in person.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            theme = "Connection",
            author = "Human Bonds"
        ),
        MotivationQuote(
            id = "q_connect_2",
            quote = "The world beyond the screen is rich with quiet wonders waiting for your gaze.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            theme = "Presence",
            author = "Natural Wonder"
        ),
        MotivationQuote(
            id = "q_connect_3",
            quote = "Giving someone your undivided eye contact is one of the highest forms of generosity.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            theme = "Undivided Attention",
            author = "Relational Health"
        ),
        MotivationQuote(
            id = "q_connect_4",
            quote = "Real life happens in the unrecorded, unshared, sacred moments of everyday life.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            theme = "Unfiltered Living",
            author = "Mindful Moments"
        ),
        MotivationQuote(
            id = "q_connect_5",
            quote = "Shared laughter in physical space releases oxytocin that no reaction emoji can match.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            theme = "Authentic Bond",
            author = "Social Neurobiology"
        ),
        MotivationQuote(
            id = "q_connect_6",
            quote = "Put the phone face down and listen to the ambient music of the room around you.",
            goal = WellnessGoal.OFFLINE_CONNECTION,
            theme = "Ambient Presence",
            author = "Mindful Listening"
        ),

        // Universal Wisdom & Daily Clarity
        MotivationQuote(
            id = "q_universal_1",
            quote = "Peace of mind is not something you find; it is something you protect.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Protection",
            author = "Quiet Wisdom"
        ),
        MotivationQuote(
            id = "q_universal_2",
            quote = "Notice when your mind feels full and allow yourself to gently put down the load.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Letting Go",
            author = "Mental Cleanse"
        ),
        MotivationQuote(
            id = "q_universal_3",
            quote = "The plan can change and still be a good, supportive plan.",
            goal = WellnessGoal.DIGITAL_BALANCE,
            theme = "Flexibility",
            author = "Adaptability"
        ),
        MotivationQuote(
            id = "q_universal_4",
            quote = "Your worth is measured by how lovingly you live, not how many notifications you clear.",
            goal = WellnessGoal.LESS_STRESS,
            theme = "Inherent Worth",
            author = "Compassionate Self"
        )
    )

    fun getDailyQuote(goal: WellnessGoal, seedDayOffset: Int = 0): MotivationQuote {
        val matching = quotes.filter { it.goal == goal }.ifEmpty { quotes }
        if (seedDayOffset > 0) {
            val randomIndex = (Math.abs(seedDayOffset * 31 + Random.nextInt(100))) % matching.size
            return matching[randomIndex]
        }
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val index = (dayOfYear + Math.abs(seedDayOffset)) % matching.size
        return matching[index]
    }

    fun getRandomQuotes(count: Int = 5, goal: WellnessGoal? = null): List<MotivationQuote> {
        val pool = if (goal != null) quotes.filter { it.goal == goal }.ifEmpty { quotes } else quotes
        return pool.shuffled().take(count)
    }
}

