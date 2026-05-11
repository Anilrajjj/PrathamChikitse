package com.prathamchikitse.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

enum class TtsStatus { IDLE, INITIALIZING, READY, PLAYING, PAUSED, ERROR, UNAVAILABLE }

data class TtsState(
    val status: TtsStatus = TtsStatus.IDLE,
    val currentUtteranceId: String? = null,
    val isKannadaAvailable: Boolean = false,
    val errorMessage: String? = null,
    val progress: Float = 0f,
    val charOffset: Int = 0,
    val totalChars: Int = 0
)

/**
 * Singleton TTS manager.
 *
 * Audio flow:
 *  1. Call [initialize] once (lifecycle-aware — call from ViewModel init).
 *  2. Call [speak] with the script + language code.
 *  3. Engine fires [UtteranceProgressListener] → status flows to UI.
 *  4. [pause] calls stop() — TTS has no resume, so resume = re-speak from current step.
 *  5. Call [shutdown] in ViewModel.onCleared() / app background.
 */
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TTSManager"
        const val UTTERANCE_PREFIX = "pratham_step_"
    }

    @Volatile
    private var tts: TextToSpeech? = null

    private val _state = MutableStateFlow(TtsState(status = TtsStatus.IDLE))
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private val onCompletedCallback = AtomicReference<(() -> Unit)?>(null)
    private var pendingText: String? = null
    private var pendingLanguage: String = "en"
    private var pendingUtteranceId: String = ""
    private var pendingSpeechRate: Float = 0.85f
    private var pendingOnCompleted: (() -> Unit)? = null
    private var resumeOffset: Int = 0

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Synchronized
    fun initialize() {
        if (_state.value.status != TtsStatus.IDLE) return
        _state.value = _state.value.copy(status = TtsStatus.INITIALIZING)

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val kannadaAvailable = checkLanguage(Locale.forLanguageTag("kn-IN"))
                _state.value = _state.value.copy(
                    status = TtsStatus.READY,
                    isKannadaAvailable = kannadaAvailable
                )
                setupProgressListener()
                Log.d(TAG, "TTS initialized. Kannada available: $kannadaAvailable")
            } else {
                _state.value = _state.value.copy(
                    status = TtsStatus.ERROR,
                    errorMessage = "TTS initialization failed (code $status)"
                )
                Log.e(TAG, "TTS init failed: $status")
            }
        }
    }

    @Synchronized
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _state.value = TtsState(status = TtsStatus.IDLE)
        onCompletedCallback.set(null)
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    /**
     * Speaks [text] using [languageCode] ("en" or "kn").
     * @param utteranceId  unique id for tracking — use step index.
     * @param speechRate   0.5 (slow) – 1.5 (fast). Default 0.85 for clarity.
     * @param onCompleted  called when utterance finishes naturally.
     */
    @Synchronized
    fun speak(
        text: String,
        languageCode: String = "en",
        utteranceId: String = UTTERANCE_PREFIX + System.currentTimeMillis(),
        speechRate: Float = 0.85f,
        onCompleted: (() -> Unit)? = null
    ) {
        val engine = tts ?: return
        if (_state.value.status == TtsStatus.ERROR ||
            _state.value.status == TtsStatus.UNAVAILABLE) return

        val locale = if (languageCode == "kn" && _state.value.isKannadaAvailable)
            Locale.forLanguageTag("kn-IN")
        else if (languageCode == "hi")
            Locale.forLanguageTag("hi-IN")
        else
            Locale.ENGLISH

        val setResult = engine.setLanguage(locale)
        if (setResult == TextToSpeech.LANG_MISSING_DATA ||
            setResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language $locale not supported, falling back to English")
            engine.setLanguage(Locale.ENGLISH)
        }

        engine.setSpeechRate(speechRate)
        engine.setPitch(1.0f)

        onCompletedCallback.set(onCompleted)
        resumeOffset = 0

        pendingText = text
        pendingLanguage = languageCode
        pendingUtteranceId = utteranceId
        pendingSpeechRate = speechRate
        pendingOnCompleted = onCompleted

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        _state.value = _state.value.copy(
            status = TtsStatus.PLAYING,
            currentUtteranceId = utteranceId,
            totalChars = text.length,
            charOffset = 0,
            progress = 0f
        )

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Resumes speech from the last paused offset.
     * Re-speaks only the remaining text from where it was paused.
     */
    @Synchronized
    fun resume() {
        val text = pendingText ?: return
        if (resumeOffset >= text.length) return

        val remainingText = text.substring(resumeOffset)
        val engine = tts ?: return

        val locale = if (pendingLanguage == "kn" && _state.value.isKannadaAvailable)
            Locale.forLanguageTag("kn-IN")
        else if (pendingLanguage == "hi")
            Locale.forLanguageTag("hi-IN")
        else
            Locale.ENGLISH

        engine.setLanguage(locale)
        engine.setSpeechRate(pendingSpeechRate)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, pendingUtteranceId)
        }

        _state.value = _state.value.copy(
            status = TtsStatus.PLAYING,
            charOffset = resumeOffset
        )

        engine.speak(remainingText, TextToSpeech.QUEUE_FLUSH, params, pendingUtteranceId)
    }

    /** Stops current speech and enters PAUSED state, saving the current offset. */
    @Synchronized
    fun pause() {
        tts?.stop()
        _state.value = _state.value.copy(status = TtsStatus.PAUSED)
    }

    /** Stops all speech and returns to READY. Clears pending text. */
    @Synchronized
    fun stop() {
        tts?.stop()
        resumeOffset = 0
        pendingText = null
        _state.value = _state.value.copy(
            status = TtsStatus.READY,
            currentUtteranceId = null,
            progress = 0f,
            charOffset = 0,
            totalChars = 0
        )
        onCompletedCallback.set(null)
    }

    val isPlaying: Boolean
        get() = _state.value.status == TtsStatus.PLAYING

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = _state.value.copy(status = TtsStatus.PLAYING)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                val total = _state.value.totalChars
                if (total > 0) {
                    resumeOffset = end
                    _state.value = _state.value.copy(
                        charOffset = end,
                        progress = end.toFloat() / total
                    )
                }
            }

            override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(
                    status = TtsStatus.READY,
                    currentUtteranceId = null,
                    progress = 1f,
                    charOffset = _state.value.totalChars
                )
                onCompletedCallback.getAndSet(null)?.invoke()
            }

            @Deprecated("Deprecated in API 21")
            override fun onError(utteranceId: String?) {
                _state.value = _state.value.copy(
                    status = TtsStatus.ERROR,
                    errorMessage = "Utterance error: $utteranceId"
                )
                onCompletedCallback.set(null)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _state.value = _state.value.copy(
                    status = TtsStatus.ERROR,
                    errorMessage = "Utterance error code $errorCode"
                )
                onCompletedCallback.set(null)
            }
        })
    }

    private fun checkLanguage(locale: Locale): Boolean {
        val result = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        return result >= TextToSpeech.LANG_AVAILABLE
    }
}
