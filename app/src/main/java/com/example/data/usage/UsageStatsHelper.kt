package com.example.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.data.local.UsageRecordEntity
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object UsageStatsHelper {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
        } catch (_: Exception) {
            AppOpsManager.MODE_DEFAULT
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun queryRealUsageForDay(
        context: Context,
        calendar: Calendar = Calendar.getInstance(),
        categoryOverrides: Map<String, AppCategory> = emptyMap()
    ): List<UsageRecordEntity> {
        if (!hasUsageAccess(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val calStart = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calEnd = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startTime = calStart.timeInMillis
        val now = System.currentTimeMillis()
        val endTime = if (calEnd.timeInMillis > now) now.coerceAtLeast(startTime + 1000) else calEnd.timeInMillis
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calStart.time)

        val pm = context.packageManager

        // Query aggregated stats for comprehensive accuracy
        val aggregatedMap = runCatching {
            usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        }.getOrNull() ?: emptyMap()

        // Fallback to queryUsageStats if needed
        val usageStatsMap = if (aggregatedMap.isNotEmpty()) {
            aggregatedMap
        } else {
            val list = runCatching {
                usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            }.getOrNull() ?: emptyList()
            list.associateBy { it.packageName }
        }

        // Query usage events for launch count and timestamps
        val eventStats = mutableMapOf<String, AppEventData>()
        runCatching {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()

            while (events != null && events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val cur = eventStats.getOrPut(pkg) { AppEventData() }
                val time = event.timeStamp

                if (cur.firstUsed == null || time < cur.firstUsed!!) {
                    cur.firstUsed = time
                }
                if (cur.lastUsed == null || time > cur.lastUsed!!) {
                    cur.lastUsed = time
                }

                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    cur.launchCount++
                }
            }
        }

        val records = mutableListOf<UsageRecordEntity>()

        for ((pkg, stat) in usageStatsMap) {
            val totalTime = stat.totalTimeInForeground
            if (totalTime < 1000) continue // Skip 0 or sub-second foreground time

            // Skip internal system noise if package is self or system UI
            if (pkg == "com.android.systemui" || pkg == context.packageName || pkg == "android") continue

            val appLabel = getAppLabel(pm, pkg)
            val category = categoryOverrides[pkg] ?: inferCategory(pkg, appLabel)
            val evtData = eventStats[pkg]

            val firstUsed = evtData?.firstUsed ?: (if (stat.firstTimeStamp > 0) stat.firstTimeStamp else null)
            val lastUsed = evtData?.lastUsed ?: (if (stat.lastTimeStamp > 0) stat.lastTimeStamp else null)
            val launchCount = evtData?.launchCount ?: if (totalTime > 0) 1 else 0

            records.add(
                UsageRecordEntity(
                    packageName = pkg,
                    appLabel = appLabel,
                    date = dateStr,
                    durationMillis = totalTime,
                    firstUsedMillis = firstUsed,
                    lastUsedMillis = lastUsed,
                    launchCount = launchCount,
                    category = category,
                    confidence = ConfidenceLevel.HIGH.name,
                    source = "SYSTEM"
                )
            )
        }

        return records.sortedByDescending { it.durationMillis }
    }

    private data class AppEventData(
        var launchCount: Int = 0,
        var firstUsed: Long? = null,
        var lastUsed: Long? = null
    )

    fun getAppLabel(pm: PackageManager, packageName: String): String {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            // Prettify common packages
            when {
                packageName.contains("youtube") -> "YouTube"
                packageName.contains("chrome") -> "Google Chrome"
                packageName.contains("whatsapp") -> "WhatsApp"
                packageName.contains("instagram") -> "Instagram"
                packageName.contains("spotify") -> "Spotify"
                packageName.contains("netflix") -> "Netflix"
                packageName.contains("tiktok") -> "TikTok"
                packageName.contains("twitter") || packageName.contains("x.android") -> "X (Twitter)"
                packageName.contains("gmail") -> "Gmail"
                packageName.contains("maps") -> "Google Maps"
                packageName.contains("slack") -> "Slack"
                packageName.contains("notion") -> "Notion"
                packageName.contains("duolingo") -> "Duolingo"
                packageName.contains("settings") -> "Settings"
                packageName.contains("camera") -> "Camera"
                else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    fun inferCategory(packageName: String, label: String): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        // 1. Development Apps
        val isDev = lowerPkg.contains("git") || lowerPkg.contains("github") || lowerPkg.contains("gitlab") ||
                lowerPkg.contains("termux") || lowerPkg.contains("terminal") || lowerPkg.contains("termius") ||
                lowerPkg.contains("code") || lowerPkg.contains("vscode") || lowerPkg.contains("replit") ||
                lowerPkg.contains("acode") || lowerPkg.contains("ide") || lowerPkg.contains("python") ||
                lowerPkg.contains("compiler") || lowerPkg.contains("debugger") || lowerPkg.contains("postman") ||
                lowerPkg.contains("docker") || lowerPkg.contains("stack") || lowerPkg.contains("android.studio") ||
                lowerPkg.contains("unity") || lowerPkg.contains("godot") || lowerPkg.contains("kotlin") ||
                lowerPkg.contains("flutter") || lowerPkg.contains("logcat") || lowerPkg.contains("dev") ||
                lowerLabel.contains("terminal") || lowerLabel.contains("code") || lowerLabel.contains("github") ||
                lowerLabel.contains("replit") || lowerLabel.contains("compiler") || lowerLabel.contains("stack overflow") ||
                lowerLabel.contains("developer") || lowerLabel.contains("ide")

        if (isDev) return AppCategory.DEVELOPMENT

        // 2. Work & Study Apps
        val isWorkStudy = lowerPkg.contains("gmail") || lowerPkg.contains("slack") || lowerPkg.contains("docs") ||
                lowerPkg.contains("sheets") || lowerPkg.contains("slides") || lowerPkg.contains("notion") ||
                lowerPkg.contains("jira") || lowerPkg.contains("confluence") || lowerPkg.contains("asana") ||
                lowerPkg.contains("trello") || lowerPkg.contains("clickup") || lowerPkg.contains("linear") ||
                lowerPkg.contains("figma") || lowerPkg.contains("miro") || lowerPkg.contains("zoom") ||
                lowerPkg.contains("teams") || lowerPkg.contains("meet") || lowerPkg.contains("classroom") ||
                lowerPkg.contains("canvas") || lowerPkg.contains("blackboard") || lowerPkg.contains("duolingo") ||
                lowerPkg.contains("coursera") || lowerPkg.contains("udemy") || lowerPkg.contains("edx") ||
                lowerPkg.contains("quizlet") || lowerPkg.contains("anki") || lowerPkg.contains("linkedin") ||
                lowerPkg.contains("office") || lowerPkg.contains("word") || lowerPkg.contains("excel") ||
                lowerPkg.contains("powerpoint") || lowerPkg.contains("pdf") || lowerPkg.contains("acrobat") ||
                lowerPkg.contains("onenote") || lowerPkg.contains("evernote") || lowerPkg.contains("outlook") ||
                lowerPkg.contains("calendar") || lowerPkg.contains("wikipedia") || lowerLabel.contains("office") ||
                lowerLabel.contains("study") || lowerLabel.contains("work") || lowerLabel.contains("docs") ||
                lowerLabel.contains("notes") || lowerLabel.contains("learn") || lowerLabel.contains("mail") ||
                lowerLabel.contains("classroom") || lowerLabel.contains("slack") || lowerLabel.contains("notion")

        if (isWorkStudy) return AppCategory.WORK_STUDY

        // 3. Tools Apps
        val isTool = lowerPkg.contains("settings") || lowerPkg.contains("calculator") || lowerPkg.contains("clock") ||
                lowerPkg.contains("alarm") || lowerPkg.contains("timer") || lowerPkg.contains("files") ||
                lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") ||
                lowerPkg.contains("drive") || lowerPkg.contains("dropbox") || lowerPkg.contains("onedrive") ||
                lowerPkg.contains("maps") || lowerPkg.contains("navigation") || lowerPkg.contains("authenticator") ||
                lowerPkg.contains("bitwarden") || lowerPkg.contains("1password") || lowerPkg.contains("keepass") ||
                lowerPkg.contains("vpn") || lowerPkg.contains("weather") || lowerPkg.contains("accessibility") ||
                lowerPkg.contains("talkback") || lowerPkg.contains("assistant") || lowerPkg.contains("voice") ||
                lowerPkg.contains("gboard") || lowerPkg.contains("keyboard") || lowerPkg.contains("scanner") ||
                lowerPkg.contains("flashlight") || lowerPkg.contains("recorder") || lowerPkg.contains("contacts") ||
                lowerPkg.contains("phone") || lowerPkg.contains("dialer") || lowerPkg.contains("systemui") ||
                lowerLabel.contains("settings") || lowerLabel.contains("tool") || lowerLabel.contains("calculator") ||
                lowerLabel.contains("clock") || lowerLabel.contains("camera") || lowerLabel.contains("maps") ||
                lowerLabel.contains("weather") || lowerLabel.contains("files") || lowerLabel.contains("vault")

        if (isTool) return AppCategory.TOOLS

        // 4. Entertainment & Media Apps (Default for social, media, video, browser, games)
        val isEntertainment = lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("disney") ||
                lowerPkg.contains("hulu") || lowerPkg.contains("primevideo") || lowerPkg.contains("twitch") ||
                lowerPkg.contains("spotify") || lowerPkg.contains("music") || lowerPkg.contains("podcast") ||
                lowerPkg.contains("audible") || lowerPkg.contains("soundcloud") || lowerPkg.contains("tiktok") ||
                lowerPkg.contains("instagram") || lowerPkg.contains("twitter") || lowerPkg.contains("x.android") ||
                lowerPkg.contains("threads") || lowerPkg.contains("reddit") || lowerPkg.contains("facebook") ||
                lowerPkg.contains("snapchat") || lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
                lowerPkg.contains("discord") || lowerPkg.contains("messenger") || lowerPkg.contains("game") ||
                lowerPkg.contains("play") || lowerPkg.contains("roblox") || lowerPkg.contains("minecraft") ||
                lowerPkg.contains("chess") || lowerPkg.contains("chrome") || lowerPkg.contains("browser") ||
                lowerPkg.contains("firefox") || lowerPkg.contains("safari") || lowerPkg.contains("edge") ||
                lowerPkg.contains("opera") || lowerPkg.contains("brave") || lowerPkg.contains("kindle") ||
                lowerLabel.contains("video") || lowerLabel.contains("game") || lowerLabel.contains("music") ||
                lowerLabel.contains("social") || lowerLabel.contains("chat") || lowerLabel.contains("stream") ||
                lowerLabel.contains("browser") || lowerLabel.contains("news")

        if (isEntertainment) return AppCategory.ENTERTAINMENT

        return AppCategory.TOOLS // Neutral utility default
    }

    // 3 rich fictional sample profiles
    fun getSampleProfilesData(profileId: String, daysCount: Int = 7): List<UsageRecordEntity> {
        val result = mutableListOf<UsageRecordEntity>()
        val calendar = Calendar.getInstance()

        for (dayOffset in 0 until daysCount) {
            val curCal = (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(curCal.time)
            val baseTime = curCal.apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            val itemsForDay = when (profileId) {
                "CREATIVE_WORK" -> listOf(
                    SampleAppItem("GitHub Mobile", "com.github.android", AppCategory.DEVELOPMENT, 45, 12, baseTime + 1000 * 60 * 45, baseTime + 1000 * 60 * 300),
                    SampleAppItem("Notion Workspace", "com.notion.android", AppCategory.WORK_STUDY, 95, 18, baseTime + 1000 * 60 * 60, baseTime + 1000 * 60 * 480),
                    SampleAppItem("Slack Team", "com.Slack", AppCategory.WORK_STUDY, 65, 45, baseTime + 1000 * 60 * 30, baseTime + 1000 * 60 * 600),
                    SampleAppItem("Google Chrome", "com.android.chrome", AppCategory.ENTERTAINMENT, 55, 22, baseTime + 1000 * 60 * 90, baseTime + 1000 * 60 * 540),
                    SampleAppItem("Spotify Music", "com.spotify.music", AppCategory.ENTERTAINMENT, 90, 8, baseTime + 1000 * 60 * 40, baseTime + 1000 * 60 * 500),
                    SampleAppItem("System Tools & Files", "com.google.android.apps.nbu.files", AppCategory.TOOLS, 15, 6, baseTime + 1000 * 60 * 120, baseTime + 1000 * 60 * 200),
                    SampleAppItem("YouTube", "com.google.android.youtube", AppCategory.ENTERTAINMENT, if (dayOffset % 2 == 0) 40 else 65, 14, baseTime + 1000 * 60 * 700, baseTime + 1000 * 60 * 850)
                )

                "STUDENT_LEARNER" -> listOf(
                    SampleAppItem("Termux Terminal", "com.termux", AppCategory.DEVELOPMENT, 50, 10, baseTime + 1000 * 60 * 60, baseTime + 1000 * 60 * 240),
                    SampleAppItem("Duolingo", "com.duolingo", AppCategory.WORK_STUDY, 35, 6, baseTime + 1000 * 60 * 30, baseTime + 1000 * 60 * 120),
                    SampleAppItem("Google Docs", "com.google.android.apps.docs.editors.docs", AppCategory.WORK_STUDY, 95, 14, baseTime + 1000 * 60 * 80, baseTime + 1000 * 60 * 420),
                    SampleAppItem("Calculator & Clock", "com.google.android.calculator", AppCategory.TOOLS, 20, 8, baseTime + 1000 * 60 * 45, baseTime + 1000 * 60 * 300),
                    SampleAppItem("Instagram", "com.instagram.android", AppCategory.ENTERTAINMENT, 75, 52, baseTime + 1000 * 60 * 60, baseTime + 1000 * 60 * 780),
                    SampleAppItem("YouTube Learning", "com.google.android.youtube", AppCategory.ENTERTAINMENT, 80, 22, baseTime + 1000 * 60 * 360, baseTime + 1000 * 60 * 820)
                )

                else -> listOf( // "EVENING_UNWINDER"
                    SampleAppItem("Acode Editor", "com.foxdebug.acode", AppCategory.DEVELOPMENT, 30, 8, baseTime + 1000 * 60 * 120, baseTime + 1000 * 60 * 240),
                    SampleAppItem("Gmail & Office", "com.google.android.gm", AppCategory.WORK_STUDY, 60, 20, baseTime + 1000 * 60 * 30, baseTime + 1000 * 60 * 480),
                    SampleAppItem("Camera & Gallery", "com.google.android.GoogleCamera", AppCategory.TOOLS, 25, 12, baseTime + 1000 * 60 * 200, baseTime + 1000 * 60 * 450),
                    SampleAppItem("YouTube Streaming", "com.google.android.youtube", AppCategory.ENTERTAINMENT, 110, 16, baseTime + 1000 * 60 * 680, baseTime + 1000 * 60 * 940),
                    SampleAppItem("Reddit", "com.reddit.frontpage", AppCategory.ENTERTAINMENT, 65, 34, baseTime + 1000 * 60 * 720, baseTime + 1000 * 60 * 920),
                    SampleAppItem("E-Reader", "com.amazon.kindle", AppCategory.ENTERTAINMENT, 40, 5, baseTime + 1000 * 60 * 850, baseTime + 1000 * 60 * 950)
                )
            }

            for (item in itemsForDay) {
                result.add(
                    UsageRecordEntity(
                        packageName = item.packageName,
                        appLabel = item.label,
                        date = dateStr,
                        durationMillis = item.durationMinutes * 60 * 1000L,
                        firstUsedMillis = item.firstUsed,
                        lastUsedMillis = item.lastUsed,
                        launchCount = item.launchCount,
                        category = item.category,
                        confidence = ConfidenceLevel.HIGH.name,
                        source = "SAMPLE"
                    )
                )
            }
        }
        return result
    }

    private data class SampleAppItem(
        val label: String,
        val packageName: String,
        val category: AppCategory,
        val durationMinutes: Long,
        val launchCount: Int,
        val firstUsed: Long,
        val lastUsed: Long
    )

    // CSV parsing helper
    fun parseCsvUsage(csvContent: String, defaultDate: String = getTodayDateString()): List<UsageRecordEntity> {
        val records = mutableListOf<UsageRecordEntity>()
        val lines = csvContent.lines().filter { it.isNotBlank() }

        for (line in lines) {
            val parts = line.split(",").map { it.trim() }
            if (parts.isEmpty() || parts[0].equals("App", ignoreCase = true) || parts[0].equals("AppName", ignoreCase = true)) {
                continue // skip header
            }

            val appName = parts.getOrNull(0) ?: continue
            val categoryStr = parts.getOrNull(1) ?: "UNKNOWN"
            val durationMinutes = parts.getOrNull(2)?.toLongOrNull() ?: 15L
            val date = parts.getOrNull(3)?.takeIf { it.isNotEmpty() } ?: defaultDate

            val category = runCatching {
                AppCategory.valueOf(categoryStr.uppercase().replace(" ", "_"))
            }.getOrDefault(inferCategory(appName, appName))

            val pkg = "imported.csv." + appName.lowercase().replace(" ", ".")

            records.add(
                UsageRecordEntity(
                    packageName = pkg,
                    appLabel = appName,
                    date = date,
                    durationMillis = durationMinutes * 60 * 1000L,
                    firstUsedMillis = null,
                    lastUsedMillis = null,
                    launchCount = null,
                    category = category,
                    confidence = ConfidenceLevel.MEDIUM.name,
                    source = "CSV"
                )
            )
        }
        return records
    }

    fun exportToCsv(records: List<UsageRecordEntity>): String {
        val sb = StringBuilder()
        sb.append("AppName,Category,DurationMinutes,Date,Launches\n")
        for (r in records) {
            val minutes = r.durationMillis / (1000 * 60)
            sb.append("${r.appLabel},${r.category.name},$minutes,${r.date},${r.launchCount ?: 0}\n")
        }
        return sb.toString()
    }

    // Screenshot OCR / Text extraction helper for Digital Wellbeing screenshot parsing
    fun parseExtractedScreenTimeText(rawText: String, defaultDate: String = getTodayDateString()): List<UsageRecordEntity> {
        val records = mutableListOf<UsageRecordEntity>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var currentAppName: String? = null
        val durationRegex = Regex("""(\d+)\s*(?:hr|hrs|h|hours?)\s*(\d+)?\s*(?:min|mins|m|minutes?)?""", RegexOption.IGNORE_CASE)
        val minutesOnlyRegex = Regex("""(\d+)\s*(?:min|mins|m|minutes?)""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val durMatch = durationRegex.find(line)
            val minMatch = minutesOnlyRegex.find(line)

            var durationMillis = 0L
            if (durMatch != null) {
                val hours = durMatch.groupValues[1].toLongOrNull() ?: 0L
                val mins = durMatch.groupValues[2].takeIf { it.isNotEmpty() }?.toLongOrNull() ?: 0L
                durationMillis = (hours * 60 + mins) * 60 * 1000L
            } else if (minMatch != null) {
                val mins = minMatch.groupValues[1].toLongOrNull() ?: 0L
                durationMillis = mins * 60 * 1000L
            }

            if (durationMillis > 0) {
                val appLabel = currentAppName ?: line.substringBefore(durMatch?.value ?: minMatch?.value ?: "").trim()
                if (appLabel.isNotBlank()) {
                    val pkg = "imported.ocr." + appLabel.lowercase().replace(Regex("[^a-z0-9]"), ".")
                    records.add(
                        UsageRecordEntity(
                            packageName = pkg,
                            appLabel = appLabel,
                            date = defaultDate,
                            durationMillis = durationMillis,
                            firstUsedMillis = null,
                            lastUsedMillis = null,
                            launchCount = null,
                            category = inferCategory(pkg, appLabel),
                            confidence = ConfidenceLevel.MEDIUM.name,
                            source = "OCR"
                        )
                    )
                }
                currentAppName = null
            } else {
                if (line.length in 2..35 && !line.contains("Digital Wellbeing", ignoreCase = true) && !line.contains("Screen time", ignoreCase = true)) {
                    currentAppName = line
                }
            }
        }
        return records
    }
}
