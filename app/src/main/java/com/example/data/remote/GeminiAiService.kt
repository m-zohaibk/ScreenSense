package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.UsageRecordEntity
import com.example.data.model.ActionEffort
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import com.example.data.model.WellnessAction
import com.example.data.model.WellnessGoal
import com.example.domain.AiBestSuggestion
import com.example.domain.AppTelemetryInsight
import com.example.domain.FocusChoice
import com.example.domain.RealtimeAiTelemetryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GeminiAnalysisResult(
    val report: RealtimeAiTelemetryReport,
    val bestSuggestions: List<AiBestSuggestion>,
    val multiChoiceFocus: List<FocusChoice>
)

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateCustomAiInsights(
        goal: WellnessGoal,
        records: List<UsageRecordEntity>,
        totalDurationFormatted: String,
        todayDate: String
    ): GeminiAnalysisResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            Log.w(TAG, "Gemini API key is not configured or is placeholder. Using smart dynamic fallback.")
            return@withContext null
        }

        if (records.isEmpty()) {
            return@withContext null
        }

        try {
            val prompt = buildAnalysisPrompt(goal, records, totalDurationFormatted, todayDate)
            val requestJson = buildRequestPayload(prompt)

            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string()
                Log.e(TAG, "Gemini API error ${response.code}: $errBody")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val rawContent = extractContentText(responseBody) ?: return@withContext null

            // Parse the JSON from the Gemini response
            parseGeminiJsonResponse(rawContent, goal, records, todayDate)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API", e)
            null
        }
    }

    private fun buildAnalysisPrompt(
        goal: WellnessGoal,
        records: List<UsageRecordEntity>,
        totalDurationFormatted: String,
        todayDate: String
    ): String {
        val currentTime = SimpleDateFormat("HH:mm, EEEE", Locale.getDefault()).format(Date())
        val appsListText = records.sortedByDescending { it.durationMillis }.take(6).joinToString("\n") { r ->
            val h = r.durationMillis / (1000 * 60 * 60)
            val m = (r.durationMillis / (1000 * 60)) % 60
            val dur = if (h > 0) "${h}h ${m}m" else "${m}m"
            "- App: ${r.appLabel} (Package: ${r.packageName}), Category: ${r.category.displayName}, Duration: $dur, Launches/Opens: ${r.launchCount ?: 1}"
        }

        return """
You are the advanced Cognitive & Digital Wellness AI for the ScreenSense Android app.
Analyze the user's real smartphone telemetry data and produce deep, personalized, highly specific behavioral insights and actionable suggestions.

CONTEXT:
- Current Time: $currentTime
- Today's Total Screen Time: $totalDurationFormatted
- User's Selected Primary Goal: ${goal.displayName} (${goal.description})
- App Telemetry Records:
$appsListText

REQUIREMENTS:
1. Provide unique, distinct, non-generic analysis for EACH app. NEVER use boilerplate or repetitive phrasing.
2. For each app, explain the specific cognitive/neurobiological impact (e.g., optical fatigue, dopamine surge loops, task-switching friction, deep flow exhaustion, blue light melatonin suppression) mentioning the app name explicitly.
3. Generate 1-2 top AI Recommendations and 3 multi-choice focus actions tailored to their exact goal (${goal.displayName}) and top used apps.
4. Output STRICTLY raw valid JSON with no conversational text or preamble.

JSON SCHEMA:
{
  "headline": "A dynamic 1-sentence analytical headline citing top app & total time",
  "overallSummary": "A 2-3 sentence personalized behavioral summary",
  "cognitiveLoadLabel": "e.g., Optimal Balance / High Visual Drain / Elevated Context Switching / Focused Flow",
  "cognitiveLoadScore": 65, // integer 0-100 (100 is max strain)
  "telemetryKeyFindings": [
    "Specific finding citing app name and exact metric 1",
    "Specific finding citing app name and exact metric 2",
    "Specific finding citing app name and exact metric 3"
  ],
  "appInsights": [
    {
      "packageName": "package.name",
      "appLabel": "App Name",
      "cognitiveImpactLevel": "High Visual Drain / Dopamine Stacking / Productive Flow / Attention Fragmentation",
      "analysisReason": "Deep specific biological/cognitive reason explaining why this duration on this specific app impacts the user",
      "actionableTakeaway": "Immediate 1-2 sentence actionable custom micro-habit for this app"
    }
  ],
  "topRecommendation": {
    "targetAppName": "Top App Name",
    "title": "Inspiring specific action title",
    "shortAction": "Clear step-by-step guidance",
    "validReason": "Comprehensive biological / behavioral justification citing usage stats",
    "expectedBenefit": "Specific cognitive, physical or emotional benefit",
    "estimatedMinutes": 3
  },
  "multiChoiceActions": [
    {
      "categoryTag": "Micro-Pause",
      "title": "Action title",
      "rationale": "Why this specific micro-pause fits their current top app load",
      "timeEstimate": "1 min"
    },
    {
      "categoryTag": "Physical Reset",
      "title": "Action title",
      "rationale": "Posture / somatic reset tailored to the device session",
      "timeEstimate": "3 min"
    },
    {
      "categoryTag": "App Boundary",
      "title": "Action title",
      "rationale": "Digital boundary for the dominant app",
      "timeEstimate": "5 min"
    }
  ]
}
""".trimIndent()
    }

    private fun buildRequestPayload(promptText: String): JSONObject {
        val root = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()

        partObj.put("text", promptText)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.3)
        genConfig.put("topP", 0.9)
        root.put("generationConfig", genConfig)

        return root
    }

    private fun extractContentText(responseJson: String): String? {
        return try {
            val root = JSONObject(responseJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val firstPart = parts.optJSONObject(0) ?: return null
            firstPart.optString("text")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting content text", e)
            null
        }
    }

    private fun parseGeminiJsonResponse(
        rawText: String,
        goal: WellnessGoal,
        records: List<UsageRecordEntity>,
        todayDate: String
    ): GeminiAnalysisResult? {
        try {
            // Clean markdown code blocks if present
            var jsonString = rawText.trim()
            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.removePrefix("```json")
            } else if (jsonString.startsWith("```")) {
                jsonString = jsonString.removePrefix("```")
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.removeSuffix("```")
            }
            jsonString = jsonString.trim()

            val json = JSONObject(jsonString)
            val totalDuration = records.sumOf { it.durationMillis }

            val headline = json.optString("headline", "AI Telemetry Telemetry Analysis")
            val overallSummary = json.optString("overallSummary", "Analysis generated by Gemini AI.")
            val cognitiveLoadLabel = json.optString("cognitiveLoadLabel", "Moderate Screen Load")
            val cognitiveLoadScore = json.optInt("cognitiveLoadScore", 50).coerceIn(5, 98)

            val findingsArray = json.optJSONArray("telemetryKeyFindings")
            val keyFindings = mutableListOf<String>()
            if (findingsArray != null) {
                for (i in 0 until findingsArray.length()) {
                    val f = findingsArray.optString(i)
                    if (f.isNotBlank()) keyFindings.add(f)
                }
            }

            // App Insights
            val appInsightsJson = json.optJSONArray("appInsights")
            val appInsightsMap = mutableMapOf<String, JSONObject>()
            if (appInsightsJson != null) {
                for (i in 0 until appInsightsJson.length()) {
                    val item = appInsightsJson.optJSONObject(i) ?: continue
                    val pkg = item.optString("packageName", "")
                    val label = item.optString("appLabel", "")
                    if (pkg.isNotBlank()) appInsightsMap[pkg] = item
                    if (label.isNotBlank()) appInsightsMap[label.lowercase()] = item
                }
            }

            val topRecords = records.sortedByDescending { it.durationMillis }.take(5)
            val generatedAppInsights = topRecords.map { r ->
                val appPercent = if (totalDuration > 0) (r.durationMillis.toFloat() / totalDuration) * 100f else 0f
                val h = r.durationMillis / (1000 * 60 * 60)
                val m = (r.durationMillis / (1000 * 60)) % 60
                val durStr = if (h > 0) "${h}h ${m}m" else "${m}m"

                val matchedJson = appInsightsMap[r.packageName] ?: appInsightsMap[r.appLabel.lowercase()]
                val impact = matchedJson?.optString("cognitiveImpactLevel") ?: "Active Engagement"
                val reason = matchedJson?.optString("analysisReason") ?: "${r.appLabel} was active for $durStr today."
                val takeaway = matchedJson?.optString("actionableTakeaway") ?: "Take a brief restorative pause before continuing."

                AppTelemetryInsight(
                    packageName = r.packageName,
                    appLabel = r.appLabel,
                    category = r.category,
                    durationMillis = r.durationMillis,
                    formattedDuration = durStr,
                    percentageOfTotal = appPercent,
                    launchCount = r.launchCount ?: 1,
                    cognitiveImpactLevel = impact,
                    analysisReason = reason,
                    actionableTakeaway = takeaway
                )
            }

            val productiveMillis = records.filter { it.category == AppCategory.WORK_STUDY || it.category == AppCategory.HEALTH_WELLNESS }.sumOf { it.durationMillis }
            val productivityRatio = if (totalDuration > 0) (productiveMillis.toFloat() / totalDuration) * 100f else 0f
            val launches = records.sumOf { it.launchCount ?: 0 }
            val contextSwitching = when {
                launches > 40 -> "High (Frequent Pickups)"
                launches > 20 -> "Moderate"
                else -> "Calm (Focused Sessions)"
            }

            val report = RealtimeAiTelemetryReport(
                headline = headline,
                overallSummary = overallSummary,
                cognitiveLoadLabel = cognitiveLoadLabel,
                cognitiveLoadScore = cognitiveLoadScore,
                topAppInsights = generatedAppInsights,
                telemetryKeyFindings = keyFindings.ifEmpty { listOf("Personalized behavioral profile computed by Gemini AI.") },
                productivityRatio = productivityRatio,
                contextSwitchingFrequency = contextSwitching
            )

            // Top Recommendation
            val topRecJson = json.optJSONObject("topRecommendation")
            val bestSuggestions = mutableListOf<AiBestSuggestion>()

            if (topRecJson != null) {
                val targetApp = topRecJson.optString("targetAppName", topRecords.firstOrNull()?.appLabel ?: "Devices")
                val title = topRecJson.optString("title", "Custom AI Focus Reset")
                val shortAction = topRecJson.optString("shortAction", "Take a short screen-free pause")
                val validReason = topRecJson.optString("validReason", "Personalized suggestion tailored to your $targetApp usage.")
                val benefit = topRecJson.optString("expectedBenefit", "Restores mental clarity and focus bandwidth.")
                val estMins = topRecJson.optInt("estimatedMinutes", 3).coerceIn(1, 15)

                val mainAction = WellnessAction(
                    id = "gemini_top_rec_$todayDate",
                    title = title,
                    description = shortAction,
                    goal = goal,
                    effortLevel = ActionEffort.TINY,
                    reason = validReason,
                    confidence = ConfidenceLevel.HIGH,
                    safetyNote = "Listen to your body and adjust comfort as needed.",
                    alternatives = listOf("Gentle deep breathing", "Hydration break", "Shoulder stretch"),
                    status = "PENDING",
                    date = todayDate
                )

                bestSuggestions.add(
                    AiBestSuggestion(
                        id = "gemini_best_sug_1",
                        badge = "✨ Gemini AI Recommendation",
                        targetAppName = targetApp,
                        title = title,
                        shortAction = shortAction,
                        validReason = validReason,
                        expectedBenefit = benefit,
                        estimatedMinutes = estMins,
                        action = mainAction
                    )
                )
            }

            // Multi Choice Actions
            val multiChoiceArray = json.optJSONArray("multiChoiceActions")
            val focusChoices = mutableListOf<FocusChoice>()

            if (multiChoiceArray != null && multiChoiceArray.length() > 0) {
                for (i in 0 until multiChoiceArray.length()) {
                    val item = multiChoiceArray.optJSONObject(i) ?: continue
                    val tag = item.optString("categoryTag", "Focus Action")
                    val title = item.optString("title", "Wellness Action")
                    val rationale = item.optString("rationale", "Designed to support your wellness.")
                    val timeEst = item.optString("timeEstimate", "2 min")

                    val act = WellnessAction(
                        id = "gemini_choice_${i}_$todayDate",
                        title = title,
                        description = rationale,
                        goal = goal,
                        effortLevel = ActionEffort.TINY,
                        reason = rationale,
                        confidence = ConfidenceLevel.HIGH,
                        safetyNote = "Take a natural pause.",
                        alternatives = listOf("Deep breath", "Water break"),
                        status = "PENDING",
                        date = todayDate
                    )

                    focusChoices.add(
                        FocusChoice(
                            id = "gemini_choice_item_${i}_$todayDate",
                            categoryTag = tag,
                            title = title,
                            rationale = rationale,
                            timeEstimate = timeEst,
                            goal = goal,
                            action = act
                        )
                    )
                }
            }

            return GeminiAnalysisResult(
                report = report,
                bestSuggestions = bestSuggestions,
                multiChoiceFocus = focusChoices
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini JSON response: $rawText", e)
            return null
        }
    }
}
