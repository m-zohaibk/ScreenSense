package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.manager.HabitTimerManager
import com.example.data.usage.UsageStatsHelper
import com.example.ui.components.MicroConfettiEffect
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenLight
import com.example.ui.theme.WarmAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitsScreen(
    habits: List<HabitEntity>,
    onToggleHabit: (HabitEntity) -> Unit,
    onPauseHabit: (HabitEntity) -> Unit,
    onSkipHabitToday: (HabitEntity) -> Unit,
    onUpdateHabit: (HabitEntity) -> Unit,
    onToggleHabitCompletion: ((HabitEntity) -> Unit)? = null,
    onAddHabit: ((HabitEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val enabledHabits = habits.filter { it.enabled }
    val availableHabits = habits.filter { !it.enabled }
    val todayDateStr = remember { UsageStatsHelper.getTodayDateString() }
    val weekDays = remember { getWeekDatesList() }

    var triggerConfetti by remember { mutableStateOf(false) }
    var habitInfoDialog by remember { mutableStateOf<HabitEntity?>(null) }
    var pausedTodayHabitId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val timerManager = remember { HabitTimerManager.getInstance(context) }
    val timerStates by timerManager.timersState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Habit Library",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Text(
                    text = "Supportive micro-actions with clear completion tracking and active background timers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Active Habits Section
            if (enabledHabits.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Active Habits (${enabledHabits.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        val completedCount = enabledHabits.count { it.completedDates.contains(todayDateStr) }
                        Surface(
                            color = if (completedCount == enabledHabits.size) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$completedCount of ${enabledHabits.size} done today",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (completedCount == enabledHabits.size) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(enabledHabits, key = { it.id }) { habit ->
                    val isCompletedToday = habit.completedDates.contains(todayDateStr)
                    val isPausedToday = pausedTodayHabitId == habit.id

                    ActiveHabitCard(
                        habit = habit,
                        isCompletedToday = isCompletedToday,
                        isPausedToday = isPausedToday,
                        weekDays = weekDays,
                        todayDateStr = todayDateStr,
                        timerManager = timerManager,
                        onToggleCompletion = {
                            if (onToggleHabitCompletion != null) {
                                onToggleHabitCompletion(habit)
                            } else {
                                val updated = if (isCompletedToday) {
                                    habit.copy(
                                        completedDates = habit.completedDates.filter { it != todayDateStr },
                                        streak = (habit.streak - 1).coerceAtLeast(0)
                                    )
                                } else {
                                    habit.copy(
                                        completedDates = habit.completedDates + todayDateStr,
                                        streak = habit.streak + 1
                                    )
                                }
                                onUpdateHabit(updated)
                            }
                            if (!isCompletedToday) {
                                triggerConfetti = true
                            }
                        },
                        onToggleReminder = {
                            onUpdateHabit(habit.copy(reminderEnabled = !habit.reminderEnabled))
                        },
                        onPauseToday = {
                            pausedTodayHabitId = if (isPausedToday) null else habit.id
                        },
                        onDeactivate = { onToggleHabit(habit) },
                        onInfoClick = { habitInfoDialog = habit }
                    )
                }
            }

            // Available Habits to Explore Section
            if (availableHabits.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explore & Add Micro-Habits",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                items(availableHabits, key = { it.id }) { habit ->
                    AvailableHabitCard(
                        habit = habit,
                        onAdd = {
                            if (onAddHabit != null) {
                                onAddHabit(habit)
                            } else {
                                onToggleHabit(habit)
                            }
                        },
                        onInfoClick = { habitInfoDialog = habit }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        MicroConfettiEffect(
            trigger = triggerConfetti,
            onAnimationEnd = { triggerConfetti = false }
        )

        // Clean Info Dialog
        if (habitInfoDialog != null) {
            val h = habitInfoDialog!!
            AlertDialog(
                onDismissRequest = { habitInfoDialog = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(h.name, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(h.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Guidance: ${h.safetyNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { habitInfoDialog = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun ActiveHabitCard(
    habit: HabitEntity,
    isCompletedToday: Boolean,
    isPausedToday: Boolean,
    weekDays: List<Pair<String, String>>,
    todayDateStr: String,
    timerManager: HabitTimerManager,
    onToggleCompletion: () -> Unit,
    onToggleReminder: () -> Unit,
    onPauseToday: () -> Unit,
    onDeactivate: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showControls by remember { mutableStateOf(false) }

    // Detect if this habit has a timer duration
    val timerDurationSecs = remember(habit.id) {
        when (habit.id) {
            "habit_eye_rest" -> 20
            "habit_movement_break" -> 180
            "habit_morning_water" -> 60
            "habit_daylight_break" -> 600
            "habit_winddown" -> 1200
            else -> 0
        }
    }

    // Connect to persistent timer manager
    val timersState by timerManager.timersState.collectAsState()
    val timerState = remember(timersState, habit.id) {
        timersState[habit.id] ?: timerManager.getOrInitTimerState(habit.id, timerDurationSecs)
    }

    // When timer finishes while running, trigger completion automatically
    LaunchedEffect(timerState.isFinished) {
        if (timerState.isFinished && !isCompletedToday && timerDurationSecs > 0) {
            onToggleCompletion()
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else if (isPausedToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Habit Icon + Title + Streak Badge + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Habit Visual Icon Indicator (Clear, distinct from radio buttons)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getHabitIcon(habit.id),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Habit info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isPausedToday) "Recovery Pause Day • Streak Preserved"
                        else habit.schedule,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPausedToday) WarmAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak Badge Pill
                if (habit.streak > 0) {
                    Surface(
                        color = WarmAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = WarmAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${habit.streak}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Direct in-card countdown timer (Persistent across app backgrounding & restart)
            if (timerDurationSecs > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (timerState.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val mins = timerState.secondsRemaining / 60
                            val secs = timerState.secondsRemaining % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d timer", mins, secs) +
                                        if (timerState.isRunning) " (Running)" else if (timerState.secondsRemaining == 0) " (Finished)" else "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (timerState.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (timerState.isRunning) {
                                        timerManager.pauseTimer(habit.id, timerDurationSecs)
                                    } else {
                                        timerManager.startTimer(habit.id, timerDurationSecs)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (timerState.isRunning) "Pause timer" else "Start timer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (timerState.secondsRemaining < timerDurationSecs || timerState.isRunning) {
                                IconButton(
                                    onClick = {
                                        timerManager.resetTimer(habit.id, timerDurationSecs)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset timer",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Consistency Tracker (Clearly labeled weekly history)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Weekly History",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEach { (label, dateKey) ->
                        val isDone = habit.completedDates.contains(dateKey)
                        val isToday = dateKey == todayDateStr

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .size(if (isToday) 20.dp else 16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) MaterialTheme.colorScheme.primary
                                        else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CLEAR DEDICATED BUTTON FOR MARKING TODAY AS COMPLETE
            if (!isCompletedToday) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleCompletion()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("mark_complete_today_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mark as Done for Today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleCompletion()
                        }
                        .testTag("completed_today_button_${habit.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✓ Completed Today (Tap to undo)",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Quick Actions & Expandable Controls
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { showControls = !showControls },
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (showControls) "Hide options ▲" else "Options ▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(visible = showControls) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleReminder,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (habit.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (habit.reminderEnabled) "Alert On" else "Alert Off",
                                fontSize = 11.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onPauseToday,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPausedToday) "Resume" else "Pause Day",
                                fontSize = 11.sp
                            )
                        }

                        TextButton(
                            onClick = onDeactivate,
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text("Remove", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

private fun getHabitIcon(habitId: String): ImageVector {
    return when (habitId) {
        "habit_eye_rest" -> Icons.Default.Visibility
        "habit_movement_break" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "habit_morning_water" -> Icons.Default.Opacity
        "habit_daylight_break" -> Icons.Default.WbSunny
        "habit_winddown" -> Icons.Default.Bedtime
        else -> Icons.Default.Spa
    }
}

@Composable
fun AvailableHabitCard(
    habit: HabitEntity,
    onAdd: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.id),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Habit info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = habit.schedule,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_habit_btn_${habit.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

fun getWeekDatesList(): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    val cal = Calendar.getInstance()
    val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = (todayDayOfWeek - Calendar.MONDAY + 7) % 7

    cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    for (i in 0..6) {
        val dateKey = format.format(cal.time)
        result.add(dayLabels[i] to dateKey)
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return result
}

