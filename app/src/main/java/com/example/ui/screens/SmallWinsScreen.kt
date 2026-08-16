package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.SmallWinEntity
import com.example.data.local.UserPreferences
import com.example.data.model.WellnessGoal
import com.example.ui.components.getGoalIcon
import com.example.ui.theme.CoralRose
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenLight
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.WarmAmber

enum class BadgeTier(val displayName: String, val color: Color) {
    BRONZE("Bronze Tier", Color(0xFFCD7F32)),
    SILVER("Silver Tier", Color(0xFF94A3B8)),
    GOLD("Gold Tier", Color(0xFFFBBF24)),
    MASTER("Master Tier", Color(0xFFA78BFA))
}

data class MilestoneBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val requiredCount: Int,
    val currentCount: Int,
    val isStreakType: Boolean,
    val tier: BadgeTier,
    val accentColor: Color
) {
    val isUnlocked: Boolean get() = currentCount >= requiredCount
    val progress: Float get() = (currentCount.toFloat() / requiredCount.toFloat()).coerceIn(0f, 1f)
}

@Composable
fun SmallWinsScreen(
    preferences: UserPreferences,
    smallWins: List<SmallWinEntity>,
    habits: List<HabitEntity> = emptyList(),
    onLogWin: (title: String, goal: WellnessGoal, status: String, reflection: String?) -> Unit,
    onDeleteWin: (Long) -> Unit,
    onSetTrackingMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedTierFilter by remember { mutableStateOf<BadgeTier?>(null) }
    var showReflectionFeedExpanded by remember { mutableStateOf(true) }

    val completedWinsCount = smallWins.count { it.status == "COMPLETED" }
    val pausedWinsCount = smallWins.count { it.status == "PAUSED" }
    val recoveryWinsCount = smallWins.count { it.status == "RECOVERY" }
    val maxStreak = habits.maxOfOrNull { it.streak } ?: 0

    val allMilestones = remember(completedWinsCount, maxStreak) {
        listOf(
            MilestoneBadge(
                id = "first_step",
                title = "First Mindful Step",
                description = "Complete your first mindful action or focus choice.",
                icon = Icons.Default.Spa,
                requiredCount = 1,
                currentCount = completedWinsCount,
                isStreakType = false,
                tier = BadgeTier.BRONZE,
                accentColor = SageGreen
            ),
            MilestoneBadge(
                id = "streak_3",
                title = "Consistency Spark",
                description = "Build a 3-day habit streak with daily mindful actions.",
                icon = Icons.Default.LocalFireDepartment,
                requiredCount = 3,
                currentCount = maxStreak,
                isStreakType = true,
                tier = BadgeTier.BRONZE,
                accentColor = WarmAmber
            ),
            MilestoneBadge(
                id = "streak_5",
                title = "Rhythm Starter",
                description = "Reach a 5-day habit streak with mindful consistency.",
                icon = Icons.Default.LocalFireDepartment,
                requiredCount = 5,
                currentCount = maxStreak,
                isStreakType = true,
                tier = BadgeTier.SILVER,
                accentColor = WarmAmber
            ),
            MilestoneBadge(
                id = "wins_10",
                title = "Decade of Wins",
                description = "Log 10 completed mindful wellness choices.",
                icon = Icons.Default.Stars,
                requiredCount = 10,
                currentCount = completedWinsCount,
                isStreakType = false,
                tier = BadgeTier.SILVER,
                accentColor = SoftCyan
            ),
            MilestoneBadge(
                id = "streak_14",
                title = "Mindful Momentum",
                description = "Maintain a steady 14-day streak of intentional device habits.",
                icon = Icons.Default.MilitaryTech,
                requiredCount = 14,
                currentCount = maxStreak,
                isStreakType = true,
                tier = BadgeTier.GOLD,
                accentColor = Color(0xFFF59E0B)
            ),
            MilestoneBadge(
                id = "wins_25",
                title = "Quarter-Century Zen",
                description = "Log 25 small wins and reflections.",
                icon = Icons.Default.EmojiEvents,
                requiredCount = 25,
                currentCount = completedWinsCount,
                isStreakType = false,
                tier = BadgeTier.GOLD,
                accentColor = Color(0xFFFBBF24)
            ),
            MilestoneBadge(
                id = "streak_30",
                title = "Digital Balance Master",
                description = "Achieve 30 consecutive days of balanced technology engagement.",
                icon = Icons.Default.WorkspacePremium,
                requiredCount = 30,
                currentCount = maxStreak,
                isStreakType = true,
                tier = BadgeTier.MASTER,
                accentColor = SoftViolet
            )
        )
    }

    val unlockedCount = allMilestones.count { it.isUnlocked }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Wins & Milestones",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                    Text(
                        text = "Celebrating every small victory and intentional pause.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showLogDialog = true },
                    modifier = Modifier.testTag("add_win_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log custom win",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Weekly Balance Summary Hero Card
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "WEEKLY BALANCE SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$completedWinsCount Wins Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMetricPill(
                            label = "Completed",
                            count = "$completedWinsCount",
                            color = SageGreen,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricPill(
                            label = "Paused Days",
                            count = "$pausedWinsCount",
                            color = WarmAmber,
                            icon = Icons.Default.PauseCircle,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricPill(
                            label = "Top Streak",
                            count = "${maxStreak}d",
                            color = SoftCyan,
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Milestone Shelf Header with Tier Progress Ring
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Milestone Achievements",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "$unlockedCount / ${allMilestones.size} Unlocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Tier Filter Chips (All, Bronze, Silver, Gold, Master)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TierFilterChip(
                    label = "All Tiers",
                    isSelected = selectedTierFilter == null,
                    onClick = { selectedTierFilter = null }
                )
                BadgeTier.values().forEach { tier ->
                    TierFilterChip(
                        label = tier.displayName.replace(" Tier", ""),
                        isSelected = selectedTierFilter == tier,
                        onClick = { selectedTierFilter = if (selectedTierFilter == tier) null else tier }
                    )
                }
            }
        }

        // Horizontal Badge Shelf Cards
        item {
            val displayedMilestones = allMilestones.filter {
                selectedTierFilter == null || it.tier == selectedTierFilter
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(displayedMilestones, key = { it.id }) { milestone ->
                    MilestoneBadgeCard(milestone = milestone)
                }
            }
        }

        // Section Header: Personal Reflection Feed (Collapsible with Category Tags)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReflectionFeedExpanded = !showReflectionFeedExpanded }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Personal Reflection Feed (${smallWins.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Icon(
                    imageVector = if (showReflectionFeedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle feed",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showReflectionFeedExpanded) {
            if (smallWins.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your Journey Starts Here",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Accept a focus choice from the Today screen or tap '+' to log your first mindful step.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(smallWins, key = { it.id }) { win ->
                    ReflectionFeedCard(
                        win = win,
                        onDelete = { onDeleteWin(win.id) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showLogDialog) {
        CustomWinLogDialog(
            defaultGoal = preferences.selectedGoal,
            onDismiss = { showLogDialog = false },
            onConfirm = { title, goal, status, reflection ->
                onLogWin(title, goal, status, reflection)
                showLogDialog = false
            }
        )
    }
}

@Composable
fun SummaryMetricPill(
    label: String,
    count: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TierFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MilestoneBadgeCard(
    milestone: MilestoneBadge,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isUnlocked) milestone.accentColor.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (milestone.isUnlocked) milestone.accentColor.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .width(190.dp)
            .testTag("milestone_card_${milestone.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (milestone.isUnlocked) milestone.accentColor.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (milestone.isUnlocked) milestone.icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (milestone.isUnlocked) milestone.accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    color = milestone.tier.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = milestone.tier.displayName.replace(" Tier", ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = milestone.tier.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = milestone.description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar & label
            LinearProgressIndicator(
                progress = { milestone.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = milestone.accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (milestone.isUnlocked) "Unlocked ✨" else "${milestone.currentCount}/${milestone.requiredCount}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (milestone.isUnlocked) milestone.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                if (!milestone.isUnlocked) {
                    Text(
                        text = "🔒 Locked",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ReflectionFeedCard(
    win: SmallWinEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate contextual category hashtag
    val hashtag = when (win.goal) {
        WellnessGoal.DIGITAL_BALANCE -> "#DigitalBoundary"
        WellnessGoal.FOCUS -> "#FocusBreak"
        WellnessGoal.SLEEP -> "#EveningWindDown"
        WellnessGoal.MOVEMENT -> "#MindfulPosture"
        WellnessGoal.LESS_STRESS -> "#MindfulPresence"
        WellnessGoal.OFFLINE_CONNECTION -> "#RealWorldConnection"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when (win.status) {
                                    "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                                    "PAUSED" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (win.status) {
                                "COMPLETED" -> Icons.Default.Check
                                "PAUSED" -> Icons.Default.PauseCircle
                                else -> Icons.Default.Spa
                            },
                            contentDescription = null,
                            tint = when (win.status) {
                                "COMPLETED" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "PAUSED" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = hashtag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = win.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete entry",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = win.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!win.reflection.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${win.reflection}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun CustomWinLogDialog(
    defaultGoal: WellnessGoal,
    onDismiss: () -> Unit,
    onConfirm: (title: String, goal: WellnessGoal, status: String, reflection: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf(defaultGoal) }
    var selectedStatus by remember { mutableStateOf("COMPLETED") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Mindful Step / Reflection", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What did you do?") },
                    placeholder = { Text("e.g. Left phone in kitchen during dinner") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("Reflection (Optional)") },
                    placeholder = { Text("How did this feel?") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Type of step:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("COMPLETED" to "Completed Win", "PAUSED" to "Mindful Pause", "RECOVERY" to "Grace Day").forEach { (status, label) ->
                        val isSelected = selectedStatus == status
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { selectedStatus = status }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedGoal, selectedStatus, reflection.trim().ifEmpty { null })
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Win")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
