package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FirebaseIntegration
import com.example.data.FlowAppPreferences
import com.example.data.FlowAudioSynthesizer
import com.example.data.FlowRepository
import com.example.data.FlowSession
import com.example.data.SmartPromptInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.R

enum class FocusState {
    NONE,       // Idle screen
    COMPASS,    // Flow Compass calibration (Challenge vs Skill)
    RITUAL,     // Threshold breathing gesture
    DEEP_SHIELD,// AMOLED focus mode
    REFLECTION  // Post-session Metacognitive Mirror
}

enum class BreathPhase {
    IN, HOLD, OUT, COMPLETE
}

enum class FlowThemeMode {
    CIRCADIAN,
    DAWN,
    DUSK,
    MIDNIGHT
}

class FlowViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FlowRepository(db.flowSessionDao())
    val appPreferences = FlowAppPreferences(application)
    val firebaseIntegration = FirebaseIntegration()

    val communityStats = firebaseIntegration.communityStats
    val aiMantra = firebaseIntegration.aiMantra
    val isAiLoading = firebaseIntegration.isAiLoading

    val suggestedIntents: StateFlow<List<String>> = appPreferences.suggestedIntents
    val activePrompt: StateFlow<SmartPromptInfo> = appPreferences.activePrompt

    // History Flow
    val allSessions: StateFlow<List<FlowSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Theme Mode Selection State
    private val _themeMode = MutableStateFlow(appPreferences.getSavedThemeMode())
    val themeMode = _themeMode.asStateFlow()

    fun setThemeMode(mode: FlowThemeMode) {
        _themeMode.value = mode
        appPreferences.saveThemeMode(mode)
    }

    fun getEffectiveThemeMode(): FlowThemeMode {
        return when (_themeMode.value) {
            FlowThemeMode.CIRCADIAN -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                when (hour) {
                    in 6..16 -> FlowThemeMode.DAWN       // 6 AM to 4:59 PM is Dawn (Light)
                    in 17..20 -> FlowThemeMode.DUSK     // 5 PM to 8:59 PM is Dusk (Slate)
                    else -> FlowThemeMode.MIDNIGHT      // 9 PM to 5:59 AM is Midnight (Pure Black)
                }
            }
            else -> _themeMode.value
        }
    }

    // Current Navigation/Focus State
    private val _focusState = MutableStateFlow(FocusState.NONE)
    val focusState = _focusState.asStateFlow()

    // Intent Input
    private val _currentIntent = MutableStateFlow("")
    val currentIntent = _currentIntent.asStateFlow()

    // Chosen Focus Duration (in minutes)
    private val _selectedDurationMinutes = MutableStateFlow(25)
    val selectedDurationMinutes = _selectedDurationMinutes.asStateFlow()

    // Countdown state (in seconds)
    private val _remainingSeconds = MutableStateFlow(1500)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    // Calibration levels (1 to 5)
    private val _challengeLevel = MutableStateFlow(3)
    val challengeLevel = _challengeLevel.asStateFlow()

    private val _skillLevel = MutableStateFlow(3)
    val skillLevel = _skillLevel.asStateFlow()

    // Sound Synthesizer
    val synthesizer = FlowAudioSynthesizer()

    // Sound Layer toggles
    private val _gammaEnabled = MutableStateFlow(false)
    val gammaEnabled = _gammaEnabled.asStateFlow()

    private val _rainEnabled = MutableStateFlow(false)
    val rainEnabled = _rainEnabled.asStateFlow()

    private val _bowlEnabled = MutableStateFlow(true) // Bowl strike active by default
    val bowlEnabled = _bowlEnabled.asStateFlow()

    // Breathing Threshold Ritual State
    private val _holdingCircle = MutableStateFlow(false)
    val holdingCircle = _holdingCircle.asStateFlow()

    private val _breathPhase = MutableStateFlow(BreathPhase.IN)
    val breathPhase = _breathPhase.asStateFlow()

    private val _breathCount = MutableStateFlow(1) // 1, 2, 3 breaths
    val breathCount = _breathCount.asStateFlow()

    private val _breathProgress = MutableStateFlow(0f) // 0.0 to 1.0 within active phase
    val breathProgress = _breathProgress.asStateFlow()

    // Timer Job
    private var countdownJob: Job? = null
    private var breathingJob: Job? = null

    // For Mindful Reminders (Stage 2)
    private val _currentReminderId = MutableStateFlow(R.string.reminder_1)
    val currentReminderId = _currentReminderId.asStateFlow()

    private val reminders = listOf(
        R.string.reminder_1,
        R.string.reminder_2,
        R.string.reminder_3,
        R.string.reminder_4,
        R.string.reminder_5,
        R.string.reminder_6,
        R.string.reminder_7,
        R.string.reminder_8
    )

    init {
        synthesizer.start(getApplication())
        updateSynthVolumes()

        viewModelScope.launch {
            com.example.service.FlowServiceController.cancelSessionRequests.collect {
                cancelSessionFlow()
            }
        }
    }

    fun setIntent(intent: String) {
        _currentIntent.value = intent
    }

    fun setDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes
        _remainingSeconds.value = minutes * 60
    }

    fun setChallengeLevel(level: Int) {
        _challengeLevel.value = level
    }

    fun setSkillLevel(level: Int) {
        _skillLevel.value = level
    }

    fun toggleGamma(enabled: Boolean) {
        _gammaEnabled.value = enabled
        updateSynthVolumes()
    }

    fun toggleRain(enabled: Boolean) {
        _rainEnabled.value = enabled
        updateSynthVolumes()
    }

    fun strikeBowl() {
        synthesizer.strikeSingingBowl()
    }

    private fun updateSynthVolumes() {
        synthesizer.gammaVolume = if (_gammaEnabled.value) 1.0f else 0.0f
        synthesizer.rainVolume = if (_rainEnabled.value) 0.8f else 0.0f
        synthesizer.bowlVolume = if (_bowlEnabled.value) 1.0f else 0.0f
    }

    // Start Focus Session Flow
    fun startCalibration() {
        _focusState.value = FocusState.COMPASS
    }

    fun proceedToRitual() {
        _focusState.value = FocusState.RITUAL
        resetBreathing()
    }

    fun cancelSessionFlow() {
        resetBreathing()
        stopFocusTimer()
        com.example.service.FlowForegroundService.stopService(getApplication())
        _focusState.value = FocusState.NONE
        appPreferences.refreshSuggestedIntents()
    }

    // Breathing Gesture Logic
    fun startHoldingBreath() {
        _holdingCircle.value = true
        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            // We need 3 full breaths. Each breath has: IN (4s), HOLD (4s), OUT (4s)
            val stepMs = 50L
            val phaseDurationMs = 4000L

            while (_holdingCircle.value) {
                // Increment phase progress
                var progress = _breathProgress.value + (stepMs.toFloat() / phaseDurationMs)
                if (progress >= 1.0f) {
                    progress = 0f
                    // Move to next phase
                    when (_breathPhase.value) {
                        BreathPhase.IN -> {
                            _breathPhase.value = BreathPhase.HOLD
                        }
                        BreathPhase.HOLD -> {
                            _breathPhase.value = BreathPhase.OUT
                        }
                        BreathPhase.OUT -> {
                            if (_breathCount.value < 3) {
                                _breathCount.value += 1
                                _breathPhase.value = BreathPhase.IN
                                synthesizer.strikeSingingBowl() // Accentuate the turn
                            } else {
                                // Completed 3 breaths!
                                _breathPhase.value = BreathPhase.COMPLETE
                                delay(200)
                                enterDeepShield()
                                break
                            }
                        }
                        else -> {}
                    }
                }
                _breathProgress.value = progress
                delay(stepMs)
            }
        }
    }

    fun releaseHoldingBreath() {
        _holdingCircle.value = false
        breathingJob?.cancel()
        // Decay breathing progress back to 0 slowly
        viewModelScope.launch {
            while (!_holdingCircle.value && _breathProgress.value > 0f) {
                _breathProgress.value = (_breathProgress.value - 0.05f).coerceAtLeast(0f)
                delay(20)
            }
        }
    }

    private fun resetBreathing() {
        _holdingCircle.value = false
        _breathPhase.value = BreathPhase.IN
        _breathCount.value = 1
        _breathProgress.value = 0f
        breathingJob?.cancel()
    }

    // Enter AMOLED Focus Mode
    fun proceedToShield() {
        enterDeepShield()
        startFocusTimer()
    }

    private fun enterDeepShield() {
        val selectedIntent = _currentIntent.value.ifBlank { "Deep Focus" }
        appPreferences.recordIntentSelection(selectedIntent)
        _focusState.value = FocusState.DEEP_SHIELD
        _remainingSeconds.value = _selectedDurationMinutes.value * 60
        _isTimerRunning.value = true
        synthesizer.strikeSingingBowl() // Strike bowl at session start!
        com.example.service.FlowForegroundService.startService(
            getApplication(),
            selectedIntent,
            _remainingSeconds.value
        )
        startFocusTimer()
    }

    private fun startFocusTimer() {
        countdownJob?.cancel()
        _isTimerRunning.value = true
        countdownJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                
                // Update foreground service notification timer
                com.example.service.FlowForegroundService.updateService(
                    getApplication(),
                    _currentIntent.value.ifBlank { "Deep Focus" },
                    _remainingSeconds.value
                )

                // Strike bowl at regular intervals to keep the mind grounded (e.g., every 5 minutes or 1 minute before completion)
                if (_remainingSeconds.value == 60) {
                    synthesizer.strikeSingingBowl() // 1 minute warning
                }
            }
            // Session complete!
            _isTimerRunning.value = false
            synthesizer.strikeSingingBowl() // Masterful finishing strike
            com.example.service.FlowForegroundService.stopService(getApplication())
            delay(1000)
            _focusState.value = FocusState.REFLECTION
        }
    }

    private fun stopFocusTimer() {
        countdownJob?.cancel()
        countdownJob = null
        _isTimerRunning.value = false
    }

    fun toggleTimerPlayPause() {
        if (_isTimerRunning.value) {
            stopFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    fun resetTimer() {
        stopFocusTimer()
        _remainingSeconds.value = _selectedDurationMinutes.value * 60
    }

    fun updateFocusInterval(minutes: Int) {
        setDuration(minutes)
        _remainingSeconds.value = minutes * 60
    }

    // Post Session Reflection
    fun saveSession(flowScore: Int, notes: String) {
        viewModelScope.launch {
            val elapsedSeconds = (_selectedDurationMinutes.value * 60) - _remainingSeconds.value
            val session = FlowSession(
                intent = _currentIntent.value.ifBlank { "Center" },
                durationSeconds = elapsedSeconds,
                flowScore = flowScore,
                challengeLevel = _challengeLevel.value,
                skillLevel = _skillLevel.value,
                notes = notes
            )
            repository.insert(session)
            
            // Sync to Firebase Cloud Data Service & Record session completed
            firebaseIntegration.syncSessionToCloud((elapsedSeconds / 60).coerceAtLeast(1))
            appPreferences.recordSessionCompleted(elapsedSeconds, flowScore)
            
            // Clean up states
            com.example.service.FlowForegroundService.stopService(getApplication())
            _currentIntent.value = ""
            _focusState.value = FocusState.NONE
            appPreferences.refreshSuggestedIntents()
        }
    }

    fun generateAiMantra() {
        viewModelScope.launch {
            firebaseIntegration.generateFocusMantra(_currentIntent.value)
        }
    }

    suspend fun getAiReflectionInsight(flowScore: Int, frictionNotes: String): String {
        return firebaseIntegration.generateReflectionInsight(
            intent = _currentIntent.value.ifBlank { "Focus Session" },
            durationMinutes = _selectedDurationMinutes.value,
            flowScore = flowScore,
            frictionNotes = frictionNotes
        )
    }

    fun discardSession() {
        _currentIntent.value = ""
        _focusState.value = FocusState.NONE
        appPreferences.refreshSuggestedIntents()
    }

    fun dismissSmartPrompt() {
        appPreferences.dismissActivePrompt()
    }

    fun onRatedApp() {
        appPreferences.onRated()
    }

    fun onNeverAskRating() {
        appPreferences.onNeverAskRating()
    }

    fun deleteSessionItem(session: FlowSession) {
        viewModelScope.launch {
            repository.delete(session)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Stage 2 Reminder Generator
    fun generateReminder() {
        var next = reminders.random()
        while (next == _currentReminderId.value) {
            next = reminders.random()
        }
        _currentReminderId.value = next
    }

    override fun onCleared() {
        super.onCleared()
        synthesizer.stop()
    }
}
