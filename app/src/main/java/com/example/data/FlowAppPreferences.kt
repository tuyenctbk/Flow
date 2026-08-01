package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow
import kotlin.random.Random

enum class SmartPromptType {
    NONE,
    RATING,
    SHARE,
    UPDATE
}

data class SmartPromptInfo(
    val type: SmartPromptType,
    val title: String = "",
    val message: String = "",
    val sessionsCompleted: Int = 0,
    val totalMinutes: Int = 0
)

class FlowAppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("flow_app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_COMPLETED_SESSIONS = "completed_sessions"
        private const val KEY_TOTAL_FOCUS_SECONDS = "total_focus_seconds"
        private const val KEY_HAS_RATED = "has_rated"
        private const val KEY_DONT_ASK_RATING = "dont_ask_rating"
        private const val KEY_LAST_RATING_PROMPT_SESSION = "last_rating_prompt_session"
        private const val KEY_LAST_SHARE_PROMPT_SESSION = "last_share_prompt_session"
        private const val KEY_LAST_UPDATE_CHECK_TIME = "last_update_check_time"
        private const val KEY_LAST_UPDATE_DISMISSED_TIME = "last_update_dismissed_time"
        private const val KEY_CUSTOM_INTENTS = "custom_intents_set"

        val DEFAULT_INTENT_POOL = listOf(
            "Deep Reading",
            "Creative Writing",
            "Mindful Reflection",
            "Strategic Planning",
            "Zen Meditation",
            "Study & Research",
            "Language Practice",
            "Visual Design",
            "Calm Breathing",
            "Solitude & Silence",
            "Problem Solving",
            "Mind Mapping",
            "Journaling",
            "Decluttering",
            "Math & Logic"
        )
    }

    private val _suggestedIntents = MutableStateFlow<List<String>>(emptyList())
    val suggestedIntents: StateFlow<List<String>> = _suggestedIntents.asStateFlow()

    private val _activePrompt = MutableStateFlow<SmartPromptInfo>(SmartPromptInfo(SmartPromptType.NONE))
    val activePrompt: StateFlow<SmartPromptInfo> = _activePrompt.asStateFlow()

    init {
        recordAppLaunch()
        refreshSuggestedIntents()
        evaluateSmartPrompts()
    }

    private fun recordAppLaunch() {
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, launches).apply()
    }

    /**
     * Record selected intent to boost its probability next time
     */
    fun recordIntentSelection(intentName: String) {
        if (intentName.isBlank()) return
        val cleanIntent = intentName.trim()
        val key = "intent_count_${cleanIntent.lowercase()}"
        val currentCount = prefs.getInt(key, 0)
        prefs.edit().putInt(key, currentCount + 1).apply()

        // Also save to custom intents set if not in default pool
        if (!DEFAULT_INTENT_POOL.any { it.equals(cleanIntent, ignoreCase = true) }) {
            val customSet = prefs.getStringSet(KEY_CUSTOM_INTENTS, emptySet())?.toMutableSet() ?: mutableSetOf()
            customSet.add(cleanIntent)
            prefs.edit().putStringSet(KEY_CUSTOM_INTENTS, customSet).apply()
        }

        refreshSuggestedIntents()
    }

    /**
     * Calculates weighted probability for each intent based on selection count
     * Next time user opens home screen, selected/frequent intents have higher chance of being picked.
     */
    fun refreshSuggestedIntents() {
        val customSet = prefs.getStringSet(KEY_CUSTOM_INTENTS, emptySet()) ?: emptySet()
        val allCandidates = (DEFAULT_INTENT_POOL + customSet).distinct()

        val weightedCandidates = allCandidates.map { intent ->
            val key = "intent_count_${intent.lowercase()}"
            val selectCount = prefs.getInt(key, 0)
            // Base weight = 1.0, each selection adds 3.0 weight multiplier
            val weight = 1.0 + (selectCount * 3.0)
            intent to weight
        }

        // Weighted random sampling without replacement (Efraimidis & Spirakis algorithm)
        val sampled = weightedCandidates.map { (intent, weight) ->
            val u = Random.nextDouble().coerceIn(0.0001, 0.9999)
            val score = u.pow(1.0 / weight)
            intent to score
        }.sortedByDescending { it.second }
            .take(4)
            .map { it.first }

        _suggestedIntents.value = sampled
    }

    fun recordSessionCompleted(durationSeconds: Int, flowScore: Int) {
        val totalSessions = prefs.getInt(KEY_COMPLETED_SESSIONS, 0) + 1
        val totalSecs = prefs.getInt(KEY_TOTAL_FOCUS_SECONDS, 0) + durationSeconds

        prefs.edit()
            .putInt(KEY_COMPLETED_SESSIONS, totalSessions)
            .putInt(KEY_TOTAL_FOCUS_SECONDS, totalSecs)
            .apply()

        evaluateSmartPrompts(flowScore)
    }

    /**
     * Smart evaluation to decide when to show Rating, Share, or Update prompts
     */
    fun evaluateSmartPrompts(latestFlowScore: Int = 0) {
        val completedSessions = prefs.getInt(KEY_COMPLETED_SESSIONS, 0)
        val totalSeconds = prefs.getInt(KEY_TOTAL_FOCUS_SECONDS, 0)
        val totalMins = totalSeconds / 60
        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        val dontAskRating = prefs.getBoolean(KEY_DONT_ASK_RATING, false)
        val lastRatingSession = prefs.getInt(KEY_LAST_RATING_PROMPT_SESSION, 0)
        val lastShareSession = prefs.getInt(KEY_LAST_SHARE_PROMPT_SESSION, 0)
        val lastUpdateCheck = prefs.getLong(KEY_LAST_UPDATE_CHECK_TIME, 0L)
        val lastUpdateDismissed = prefs.getLong(KEY_LAST_UPDATE_DISMISSED_TIME, 0L)
        val now = System.currentTimeMillis()

        // 1. App Update Suggestion Prompt (Smart check: after 5 app opens or 1 day since last check)
        val updateAvailable = checkAppUpdateAvailable(now, lastUpdateCheck)
        if (updateAvailable && (now - lastUpdateDismissed > 24 * 3600 * 1000L)) {
            _activePrompt.value = SmartPromptInfo(
                type = SmartPromptType.UPDATE,
                title = "New Version 1.2.0 Available",
                message = "Enhanced ambient audio synthesis, improved breathing rituals, and smoother animations."
            )
            return
        }

        // 2. 5-Star Rating Prompt
        // Triggers if completed >= 2 sessions, hasn't rated, hasn't opted out, and >= 3 sessions since last ask
        if (!hasRated && !dontAskRating && completedSessions >= 2 && (completedSessions - lastRatingSession >= 3)) {
            if (latestFlowScore >= 4 || completedSessions >= 3) {
                _activePrompt.value = SmartPromptInfo(
                    type = SmartPromptType.RATING,
                    title = "Finding Stillness in FLOW?",
                    message = "If FLOW helps you cultivate focus and calm, a 5-star rating helps others discover it too.",
                    sessionsCompleted = completedSessions,
                    totalMinutes = totalMins
                )
                prefs.edit().putInt(KEY_LAST_RATING_PROMPT_SESSION, completedSessions).apply()
                return
            }
        }

        // 3. Share App Suggestion Prompt
        // Triggers on milestone sessions or high flow score
        if (completedSessions >= 3 && (completedSessions - lastShareSession >= 3)) {
            if (latestFlowScore >= 4 || completedSessions % 3 == 0) {
                _activePrompt.value = SmartPromptInfo(
                    type = SmartPromptType.SHARE,
                    title = "Share Your Focus Milestone",
                    message = "You've completed $completedSessions focus sessions ($totalMins mins total in flow). Invite friends to focus with you!",
                    sessionsCompleted = completedSessions,
                    totalMinutes = totalMins
                )
                prefs.edit().putInt(KEY_LAST_SHARE_PROMPT_SESSION, completedSessions).apply()
                return
            }
        }

        _activePrompt.value = SmartPromptInfo(SmartPromptType.NONE)
    }

    private fun checkAppUpdateAvailable(now: Long, lastCheckTime: Long): Boolean {
        prefs.edit().putLong(KEY_LAST_UPDATE_CHECK_TIME, now).apply()
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val daysSinceFirstCheck = (now - lastCheckTime) / (1000 * 3600 * 24)
        return launches >= 5 && daysSinceFirstCheck >= 1
    }

    fun dismissActivePrompt() {
        if (_activePrompt.value.type == SmartPromptType.UPDATE) {
            prefs.edit().putLong(KEY_LAST_UPDATE_DISMISSED_TIME, System.currentTimeMillis()).apply()
        }
        _activePrompt.value = SmartPromptInfo(SmartPromptType.NONE)
    }

    fun onRated() {
        prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
        _activePrompt.value = SmartPromptInfo(SmartPromptType.NONE)
    }

    fun onNeverAskRating() {
        prefs.edit().putBoolean(KEY_DONT_ASK_RATING, true).apply()
        _activePrompt.value = SmartPromptInfo(SmartPromptType.NONE)
    }
}
