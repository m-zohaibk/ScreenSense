package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.ActionEffort
import com.example.data.model.ConfidenceLevel
import com.example.data.model.WellnessAction
import com.example.data.model.WellnessGoal
import com.example.domain.UsageAnalysisSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiWellnessService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val MODEL_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    suspend fun personalizeWellnessAction(
        baseAction: WellnessAction,
        goal: WellnessGoal,
        summary: UsageAnalysisSummary
    ): WellnessAction = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            return@withContext baseAction // Seamless local fallback
        }

        try {
            val systemPrompt = """
                You are ScreenSense AI, a gentle, non-judgmental digital wellness assistant.
                You help users understand one app usage pattern and suggest one small supportive choice based on their goal: ${goal.displayName}.
                
                CRITICAL SAFETY & TONE RULES:
                1. NEVER judge, shame, scold, or use alarming words (no 'addicted', 'unhealthy', 'wasted day').
                2. NEVER diagnose, prescribe medication, or act as a therapist/doctor.
                3. Treat screen time as neutral (work, study, and connection are valid).
                4. Output STRICT JSON only with fields:
                   - "title": String (concise, supportive action title)
                   - "description": String (1-2 clear, actionable, friendly sentences)
                   - "reason": String (explanation tied directly to visible data)
                   - "safetyNote": String (gentle reassurance or physical safety reminder)
                   - "alternatives": Array of 3 short alternative action strings
            """.trimIndent()

            val userMessage = """
                User's Selected Goal: ${goal.displayName}
                Dominant Category: ${summary.dominantCategory.displayName} (${summary.categoryPercentages[summary.dominantCategory]?.toInt() ?: 0}% of visible time)
                Total Screen Duration: ${summary.formattedTotalTime}
                Has Evening Screen Time: ${summary.hasEveningUsage}
                Has Frequent Short Opens: ${summary.hasFrequentChecking}
                Has Long Session: ${summary.hasLongSession}
                Base Suggestion: ${baseAction.title} - ${baseAction.description}
                
                Please refine this action into a calm, encouraging suggestion tailored to their goal.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userMessage))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("$MODEL_ENDPOINT?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext baseAction
            }

            val responseBody = response.body?.string() ?: return@withContext baseAction
            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return@withContext baseAction
            val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return@withContext baseAction
            val parts = content.optJSONArray("parts") ?: return@withContext baseAction
            val text = parts.optJSONObject(0)?.optString("text") ?: return@withContext baseAction

            val parsedOutput = JSONObject(text)
            val title = parsedOutput.optString("title", baseAction.title)
            val desc = parsedOutput.optString("description", baseAction.description)
            val reason = parsedOutput.optString("reason", baseAction.reason)
            val safetyNote = parsedOutput.optString("safetyNote", baseAction.safetyNote ?: "")
            val altJsonArray = parsedOutput.optJSONArray("alternatives")
            val alternatives = mutableListOf<String>()
            if (altJsonArray != null) {
                for (i in 0 until altJsonArray.length()) {
                    alternatives.add(altJsonArray.getString(i))
                }
            }

            baseAction.copy(
                title = title.ifBlank { baseAction.title },
                description = desc.ifBlank { baseAction.description },
                reason = reason.ifBlank { baseAction.reason },
                safetyNote = if (safetyNote.isNotBlank()) safetyNote else baseAction.safetyNote,
                alternatives = if (alternatives.isNotEmpty()) alternatives else baseAction.alternatives,
                confidence = ConfidenceLevel.HIGH
            )
        } catch (e: Exception) {
            baseAction // Fail-safe local fallback
        }
    }
}
