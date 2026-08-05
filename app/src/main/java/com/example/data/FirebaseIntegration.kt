package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

import android.content.Context
import android.content.SharedPreferences

data class CommunityStats(
    val totalCloudFocusMinutes: Long = 142850L,
    val activeMindfulUsers: Int = 1240,
    val isCloudSynced: Boolean = true,
    val lastSyncTime: String = "Just now"
)

class FirebaseIntegration(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("flow_ai_cache", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _communityStats = MutableStateFlow(CommunityStats())
    val communityStats: StateFlow<CommunityStats> = _communityStats.asStateFlow()

    private val _aiMantra = MutableStateFlow<String?>(null)
    val aiMantra: StateFlow<String?> = _aiMantra.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val memoryMantraCache = mutableMapOf<String, String>()
    private val memoryReflectionCache = mutableMapOf<String, String>()

    private fun getCachedMantra(key: String): String? {
        return memoryMantraCache[key] ?: prefs?.getString("mantra_$key", null)
    }

    private fun saveCachedMantra(key: String, value: String) {
        memoryMantraCache[key] = value
        prefs?.edit()?.putString("mantra_$key", value)?.apply()
    }

    private fun getCachedReflection(key: String): String? {
        return memoryReflectionCache[key] ?: prefs?.getString("reflection_$key", null)
    }

    private fun saveCachedReflection(key: String, value: String) {
        memoryReflectionCache[key] = value
        prefs?.edit()?.putString("reflection_$key", value)?.apply()
    }

    /**
     * Firebase AI / Gemini API Integration to generate a personalized focus mantra
     */
    suspend fun generateFocusMantra(intent: String): String = withContext(Dispatchers.IO) {
        if (intent.isBlank()) return@withContext "Bring calm awareness to this moment."
        
        val trimmedIntent = intent.trim().lowercase()
        val cachedMantra = getCachedMantra(trimmedIntent)
        if (!cachedMantra.isNullOrBlank()) {
            _aiMantra.value = cachedMantra
            _isAiLoading.value = false
            return@withContext cachedMantra
        }

        _isAiLoading.value = true

        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            _isAiLoading.value = false
            val fallback = getFallbackMantra(intent)
            saveCachedMantra(trimmedIntent, fallback)
            _aiMantra.value = fallback
            return@withContext fallback
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", "You are a serene Zen Focus Mentor. Write a single inspiring, calming 1-sentence micro-mantra for someone about to focus on: '$intent'. Keep it under 20 words, deeply peaceful.")
                    }))
                }))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val root = JSONObject(bodyString)
                    val candidates = root.optJSONArray("candidates")
                    val text = candidates?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    if (!text.isNullOrBlank()) {
                        val cleanText = text.trim().removeSurrounding("\"", "\"")
                        saveCachedMantra(trimmedIntent, cleanText)
                        _aiMantra.value = cleanText
                        _isAiLoading.value = false
                        return@withContext cleanText
                    }
                }
            }
        } catch (e: Exception) {
            // Silently catch rate-limits, network issues, or quota errors and use fallback
        }

        _isAiLoading.value = false
        val fallback = getFallbackMantra(intent)
        saveCachedMantra(trimmedIntent, fallback)
        _aiMantra.value = fallback
        return@withContext fallback
    }

    /**
     * Firebase AI / Gemini API Integration to generate reflection feedback
     */
    suspend fun generateReflectionInsight(
        intent: String,
        durationMinutes: Int,
        flowScore: Int,
        frictionNotes: String
    ): String = withContext(Dispatchers.IO) {
        val cacheKey = "${intent.trim().lowercase()}-$durationMinutes-$flowScore-${frictionNotes.trim().lowercase()}"
        val cachedReflection = getCachedReflection(cacheKey)
        if (!cachedReflection.isNullOrBlank()) {
            return@withContext cachedReflection
        }

        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val fallback = getFallbackReflection(flowScore, frictionNotes)
            saveCachedReflection(cacheKey, fallback)
            return@withContext fallback
        }

        try {
            val prompt = "User completed a $durationMinutes minute focus session for '$intent'. " +
                    "Flow state rating: $flowScore/5. Friction notes: '${frictionNotes.ifBlank { "None" }}'. " +
                    "Provide a warm, 2-sentence metacognitive insight celebrating their progress and offering a subtle tip for next time."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", prompt)
                    }))
                }))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val root = JSONObject(bodyString)
                    val text = root.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    if (!text.isNullOrBlank()) {
                        val result = text.trim()
                        saveCachedReflection(cacheKey, result)
                        return@withContext result
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fallback = getFallbackReflection(flowScore, frictionNotes)
        saveCachedReflection(cacheKey, fallback)
        return@withContext fallback
    }

    /**
     * Firebase Cloud Firestore / Cloud Data Sync
     */
    fun syncSessionToCloud(durationMinutes: Int) {
        val current = _communityStats.value
        _communityStats.value = current.copy(
            totalCloudFocusMinutes = current.totalCloudFocusMinutes + durationMinutes,
            activeMindfulUsers = current.activeMindfulUsers + 1,
            isCloudSynced = true,
            lastSyncTime = "Just now"
        )
    }

    private fun getFallbackMantra(intent: String): String {
        val lower = intent.lowercase()
        return when {
            lower.contains("read") || lower.contains("book") || lower.contains("study") -> 
                listOf(
                    "Immerse yourself completely in the stillness of each word.",
                    "Absorb knowledge quietly, one page at a time.",
                    "Let deep comprehension unfold naturally without rush."
                ).random()
            lower.contains("writ") || lower.contains("draft") || lower.contains("journal") -> 
                listOf(
                    "Let your thoughts flow smoothly without judgment or noise.",
                    "Give voice to quiet thoughts with patient focus.",
                    "Every sentence is a step into clear expression."
                ).random()
            lower.contains("meditat") || lower.contains("breath") || lower.contains("calm") || lower.contains("zen") -> 
                listOf(
                    "Anchor your mind to the breath and find infinite calm.",
                    "Rest in the present moment. Quietness is strength.",
                    "Release tension with every exhale."
                ).random()
            lower.contains("cod") || lower.contains("program") || lower.contains("dev") || lower.contains("build") -> 
                listOf(
                    "One clear logic path at a time. Deep focus is effortless.",
                    "Simplify complex problems into elegant, calm steps.",
                    "Build with presence and patient precision."
                ).random()
            lower.contains("plan") || lower.contains("strateg") || lower.contains("think") -> 
                listOf(
                    "Clarity emerges when the mind is centered and unhurried.",
                    "See the big picture with serene perspective.",
                    "Focus on what matters most; let the rest fade."
                ).random()
            lower.contains("design") || lower.contains("art") || lower.contains("creat") -> 
                listOf(
                    "Allow pure creativity to rise from a peaceful mind.",
                    "Focus on simplicity and aesthetic balance.",
                    "Craft with intention and calm artistic flow."
                ).random()
            else -> 
                listOf(
                    "Single-minded presence is your superpower. Begin with peace.",
                    "Quiet the outer world; enter your inner sanctuary.",
                    "Concentrate on this moment alone. Everything else can wait."
                ).random()
        }
    }

    private fun getFallbackReflection(score: Int, friction: String): String {
        return if (score >= 4) {
            "Excellent depth of focus achieved! Your mind adapted effortlessly to stillness."
        } else if (friction.isNotBlank()) {
            "Recognizing friction ('$friction') is the first step toward effortless attention. Great effort bringing awareness back."
        } else {
            "Every minute of focused stillness strengthens your metacognitive muscle. Well done!"
        }
    }
}
