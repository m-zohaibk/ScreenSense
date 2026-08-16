package com.example.data.model

import com.example.data.local.HabitEntity

object HabitDefaults {
    fun getDefaultHabits(): List<HabitEntity> = listOf(
        HabitEntity(
            id = "habit_winddown",
            name = "Screen-Free Wind-Down",
            description = "A gentle 20-minute screen buffer before sleep to let your mind settle naturally.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Nightly • 10:00 PM",
            safetyNote = "Adjust the duration to whatever feels realistic; even 5 minutes makes a positive difference.",
            goalHint = WellnessGoal.SLEEP
        ),
        HabitEntity(
            id = "habit_morning_water",
            name = "Morning Water",
            description = "Enjoy a refreshing glass of water upon waking before checking morning notifications.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Morning • 7:30 AM",
            safetyNote = "Drink at your own comfortable pace; there is no rigid volume requirement.",
            goalHint = WellnessGoal.DIGITAL_BALANCE
        ),
        HabitEntity(
            id = "habit_daylight_break",
            name = "Daylight Break",
            description = "Spend 10 minutes outdoors or near a sunlit window during the day for natural circadian rhythm.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Midday • 12:30 PM",
            safetyNote = "Never look directly at the sun. Indoor natural window light is a great alternative.",
            goalHint = WellnessGoal.LESS_STRESS
        ),
        HabitEntity(
            id = "habit_movement_break",
            name = "Three-Minute Movement",
            description = "A quick, seated or standing stretch to release neck, shoulder, and back tension.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Afternoon • 3:00 PM",
            safetyNote = "Stop immediately if any stretch causes discomfort or pain.",
            goalHint = WellnessGoal.MOVEMENT
        ),
        HabitEntity(
            id = "habit_eye_rest",
            name = "Eye-Rest Break (20-20 Rule)",
            description = "Look at an object at least 20 feet away for 20 seconds to relax ciliary eye muscles.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Every 2 hours",
            safetyNote = "Softly blink several times to replenish natural tear film.",
            goalHint = WellnessGoal.FOCUS
        ),
        HabitEntity(
            id = "habit_refreshing_shower",
            name = "Refreshing Shower",
            description = "Take a warm, normal, or refreshing shower to transition between workday and evening relaxation.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Evening • 6:30 PM",
            safetyNote = "Choose the temperature you find most soothing and comfortable.",
            goalHint = WellnessGoal.LESS_STRESS
        ),
        HabitEntity(
            id = "habit_one_good_thing",
            name = "One Good Thing",
            description = "Jot down or reflect on one moment, idea, or small kindness you appreciated today.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Evening • 9:00 PM",
            safetyNote = "No forced toxic positivity; everyday simple things count completely.",
            goalHint = WellnessGoal.OFFLINE_CONNECTION
        ),
        HabitEntity(
            id = "habit_small_connection",
            name = "Small Connection",
            description = "Send a warm text, check in with a friend, or spend quiet time with a pet or loved one.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Daily • 5:00 PM",
            safetyNote = "Keep it low-pressure without expectation of immediate back-and-forth.",
            goalHint = WellnessGoal.OFFLINE_CONNECTION
        ),
        HabitEntity(
            id = "habit_planned_entertainment",
            name = "Planned Entertainment Window",
            description = "Dedicate an intentional block of time for shows, gaming, or videos without feeling guilty.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Evening • 7:30 PM",
            safetyNote = "Enjoy your entertainment mindfully rather than as mindless background noise.",
            goalHint = WellnessGoal.DIGITAL_BALANCE
        ),
        HabitEntity(
            id = "habit_study_work_break",
            name = "Study or Work Break",
            description = "Step completely away from your desk for 5 minutes of quiet, water, or walking after long tasks.",
            enabled = false,
            reminderEnabled = false,
            schedule = "Workdays • Mid-morning",
            safetyNote = "Leaving the work room physically helps the brain switch modes.",
            goalHint = WellnessGoal.FOCUS
        )
    )
}
