package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.usage.UsageStatsHelper
import com.example.ui.screens.DataPrivacyScreen
import com.example.ui.screens.GoalSelectionDialog
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PatternsScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.SmallWinsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.ScreenSenseTheme
import com.example.ui.viewmodel.ScreenSenseViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TODAY("Today", Icons.Filled.Spa, Icons.Outlined.Spa, "tab_today"),
    PATTERNS("Patterns", Icons.Filled.Insights, Icons.Outlined.Insights, "tab_patterns"),
    HABITS("Habits", Icons.Filled.Checklist, Icons.Outlined.Checklist, "tab_habits"),
    WINS("Wins", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "tab_wins"),
    QUOTES("Mindful", Icons.Filled.FormatQuote, Icons.Outlined.FormatQuote, "tab_quotes"),
    PRIVACY("Privacy", Icons.Filled.Security, Icons.Outlined.Security, "tab_privacy")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ScreenSenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPrefs by viewModel.userPreferences.collectAsState()

            val isDarkTheme = when (userPrefs.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            ScreenSenseTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!userPrefs.isOnboardingCompleted) {
                        OnboardingScreen(
                            selectedGoal = userPrefs.selectedGoal,
                            onSelectGoal = { viewModel.selectGoal(it) },
                            onFinishOnboarding = { useSample ->
                                viewModel.completeOnboarding(useSample)
                            }
                        )
                    } else {
                        MainContent(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkUsageAccess()
        viewModel.refreshUsage()
    }
}

@Composable
fun MainContent(viewModel: ScreenSenseViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val userPrefs by viewModel.userPreferences.collectAsState()
    val todayRecords by viewModel.todayUsageRecords.collectAsState()
    val todayAction by viewModel.todayAction.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val smallWins by viewModel.smallWins.collectAsState()
    val analysisSummary by viewModel.analysisSummary.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val hasUsageAccess by viewModel.hasUsageAccess.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val geminiResult by viewModel.geminiResult.collectAsState()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsState()

    val context = LocalContext.current

    if (showGoalDialog) {
        GoalSelectionDialog(
            currentGoal = userPrefs.selectedGoal,
            onDismiss = { showGoalDialog = false },
            onGoalSelected = { viewModel.selectGoal(it) }
        )
    }

    Scaffold(
        bottomBar = {
            Column {
                androidx.compose.material3.HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    NavigationTab.values().forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> TodayScreen(
                        preferences = userPrefs,
                        analysisSummary = analysisSummary,
                        todayAction = todayAction,
                        smallWins = smallWins,
                        habits = habits,
                        usageRecords = todayRecords,
                        hasUsageAccess = hasUsageAccess,
                        isAiGenerating = isAiGenerating,
                        geminiResult = geminiResult,
                        aiStatusMessage = aiStatusMessage,
                        onRegenerateAi = { viewModel.regenerateAiInsights() },
                        onRequestUsageAccess = {
                            runCatching {
                                context.startActivity(UsageStatsHelper.getUsageAccessSettingsIntent())
                            }
                        },
                        onSetSampleMode = { viewModel.setSampleDataMode(it) },
                        onAcceptAction = { viewModel.acceptAction(it) },
                        onPostponeAction = { viewModel.postponeAction(it) },
                        onDismissAction = { viewModel.dismissAction(it) },
                        onRefresh = { viewModel.refreshUsage() },
                        onNavigateToGoals = { showGoalDialog = true },
                        onNavigateToSmallWins = { selectedTab = 3 },
                        onNavigateToMindful = { selectedTab = 4 }
                    )
                    1 -> PatternsScreen(
                        analysisSummary = analysisSummary,
                        usageRecords = todayRecords,
                        onUpdateCategory = { pkg, cat -> viewModel.updateAppCategory(pkg, cat) }
                    )
                    2 -> HabitsScreen(
                        habits = habits,
                        onToggleHabit = { viewModel.toggleHabit(it) },
                        onPauseHabit = { viewModel.pauseHabit(it) },
                        onSkipHabitToday = { viewModel.skipHabitToday(it) },
                        onUpdateHabit = { viewModel.updateHabit(it) },
                        onToggleHabitCompletion = { viewModel.toggleHabitCompletion(it) },
                        onAddHabit = { viewModel.addHabit(it) }
                    )
                    3 -> SmallWinsScreen(
                        preferences = userPrefs,
                        smallWins = smallWins,
                        habits = habits,
                        onLogWin = { title, goal, status, ref ->
                            viewModel.logCustomSmallWin(title, goal, status, ref)
                        },
                        onDeleteWin = { viewModel.deleteSmallWin(it) },
                        onSetTrackingMode = { viewModel.setProgressTrackingMode(it) }
                    )
                    4 -> QuotesScreen(
                        quote = dailyQuote,
                        selectedGoal = userPrefs.selectedGoal,
                        onRefreshQuote = { viewModel.refreshQuote() }
                    )
                    5 -> DataPrivacyScreen(
                        preferences = userPrefs,
                        hasUsageAccess = hasUsageAccess,
                        onRefreshAccess = { viewModel.checkUsageAccess() },
                        onSetSampleProfile = { viewModel.setSampleProfile(it) },
                        onSetSampleDataMode = { viewModel.setSampleDataMode(it) },
                        onDeleteUsageData = { viewModel.deleteAllUsageData() },
                        onDeleteAllData = { viewModel.deleteAllData() },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetReducedMotion = { viewModel.setReducedMotion(it) }
                    )
                }
            }
        }
    }
}
