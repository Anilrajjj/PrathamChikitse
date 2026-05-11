package com.prathamchikitse.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.EmergencyStep
import com.prathamchikitse.domain.usecase.GetEmergencyDetailUseCase
import com.prathamchikitse.domain.usecase.GetUserPreferencesUseCase
import com.prathamchikitse.navigation.Screen
import com.prathamchikitse.tts.TTSManager
import com.prathamchikitse.tts.TtsState
import com.prathamchikitse.tts.TtsStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val emergency: Emergency? = null,
    val currentStepIndex: Int = 0,
    val isLoading: Boolean = true,
    val currentLanguage: String = "en",
    val error: String? = null
)

@HiltViewModel
class EmergencyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEmergencyDetail: GetEmergencyDetailUseCase,
    private val getUserPreferences: GetUserPreferencesUseCase,
    val ttsManager: TTSManager
) : ViewModel() {

    private val emergencyId: String =
        checkNotNull(savedStateHandle[Screen.EmergencyDetail.ARG_ID])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val ttsState: StateFlow<TtsState> = ttsManager.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, TtsState())

    val preferences = getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        ttsManager.initialize()
        loadEmergency()
    }

    private fun loadEmergency() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val emergency = getEmergencyDetail(emergencyId)
                val prefs = getUserPreferences().first()
                _uiState.update { state ->
                    state.copy(
                        emergency = emergency,
                        isLoading = false,
                        currentLanguage = prefs.languageCode
                    )
                }
                if (emergency != null && prefs.ttsEnabled) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(1000)
                        autoPlayCurrentStep()
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Step navigation ───────────────────────────────────────────────────────

    fun goToStep(index: Int) {
        ttsManager.stop()
        val maxIndex = (_uiState.value.emergency?.steps?.size ?: 1) - 1
        _uiState.update { it.copy(currentStepIndex = index.coerceIn(0, maxIndex)) }
        val prefs = preferences.value
        if (prefs?.ttsEnabled == true) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(300)
                autoPlayCurrentStep()
            }
        }
    }

    fun nextStep() {
        val current = _uiState.value
        val max = (current.emergency?.steps?.size ?: 1) - 1
        if (current.currentStepIndex < max) {
            goToStep(current.currentStepIndex + 1)
        }
    }

    fun prevStep() {
        val current = _uiState.value
        if (current.currentStepIndex > 0) {
            goToStep(current.currentStepIndex - 1)
        }
    }

    // ── TTS controls ──────────────────────────────────────────────────────────

    fun speakCurrentStep() {
        val state = _uiState.value
        val step = state.emergency?.steps?.getOrNull(state.currentStepIndex) ?: return
        val lang = state.currentLanguage
        val script = if (lang == "kn") step.audioScriptKn else step.audioScriptEn
        val prefs = preferences.value

        ttsManager.speak(
            text = script,
            languageCode = lang,
            utteranceId = "${TTSManager.UTTERANCE_PREFIX}${state.currentStepIndex}",
            speechRate = prefs?.ttsSpeedRate ?: 0.85f,
            onCompleted = {
                if (state.currentStepIndex < (state.emergency.steps.size - 1)) {
                    nextStep()
                    speakCurrentStep()
                }
            }
        )
    }

    private fun autoPlayCurrentStep() {
        val state = _uiState.value
        if (state.isLoading || state.emergency == null) return
        val step = state.emergency.steps.getOrNull(state.currentStepIndex) ?: return
        val prefs = preferences.value
        if (prefs?.ttsEnabled != true) return

        val lang = state.currentLanguage
        val script = if (lang == "kn") step.audioScriptKn else step.audioScriptEn
        ttsManager.speak(
            text = script,
            languageCode = lang,
            utteranceId = "${TTSManager.UTTERANCE_PREFIX}${state.currentStepIndex}",
            speechRate = prefs.ttsSpeedRate,
            onCompleted = {
                val currentState = _uiState.value
                if (currentState.currentStepIndex < (currentState.emergency?.steps?.size ?: 1) - 1) {
                    goToStep(currentState.currentStepIndex + 1)
                    autoPlayCurrentStep()
                }
            }
        )
    }

    fun pauseTts() = ttsManager.pause()

    fun stopTts() = ttsManager.stop()

    fun toggleTts() {
        val prefs = preferences.value
        if (prefs?.ttsEnabled == false) return
        when (ttsManager.state.value.status) {
            TtsStatus.PLAYING -> pauseTts()
            TtsStatus.PAUSED -> ttsManager.resume()
            TtsStatus.READY -> speakCurrentStep()
            else -> Unit
        }
    }

    fun setLanguage(code: String) {
        ttsManager.stop()
        _uiState.update { it.copy(currentLanguage = code) }
    }

    override fun onCleared() {
        ttsManager.shutdown()
        super.onCleared()
    }
}
