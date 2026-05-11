package com.prathamchikitse.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.prathamchikitse.domain.model.UserPreferences
import com.prathamchikitse.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    companion object {
        val KEY_LANGUAGE     = stringPreferencesKey("language")
        val KEY_DARK_MODE    = booleanPreferencesKey("dark_mode")
        val KEY_FONT_SIZE    = floatPreferencesKey("font_size")
        val KEY_TTS_ENABLED  = booleanPreferencesKey("tts_enabled")
        val KEY_TTS_SPEED    = floatPreferencesKey("tts_speed")
        val KEY_ONBOARDING   = booleanPreferencesKey("has_completed_onboarding")
    }

    override fun getUserPreferences(): Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            UserPreferences(
                languageCode          = prefs[KEY_LANGUAGE] ?: "en",
                isDarkMode            = prefs[KEY_DARK_MODE] ?: true,
                fontSizeMultiplier    = prefs[KEY_FONT_SIZE] ?: 1.0f,
                ttsEnabled            = prefs[KEY_TTS_ENABLED] ?: true,
                ttsSpeedRate          = prefs[KEY_TTS_SPEED] ?: 0.85f,
                hasCompletedOnboarding = prefs[KEY_ONBOARDING] ?: false
            )
        }

    override suspend fun updateLanguage(code: String) {
        dataStore.edit { it[KEY_LANGUAGE] = code }
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    override suspend fun updateFontSize(multiplier: Float) {
        dataStore.edit { it[KEY_FONT_SIZE] = multiplier }
    }

    override suspend fun updateTtsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_TTS_ENABLED] = enabled }
    }

    override suspend fun updateTtsSpeed(rate: Float) {
        dataStore.edit { it[KEY_TTS_SPEED] = rate }
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING] = completed }
    }
}
