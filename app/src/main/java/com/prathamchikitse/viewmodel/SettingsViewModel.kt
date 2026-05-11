package com.prathamchikitse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prathamchikitse.domain.model.UserPreferences
import com.prathamchikitse.domain.usecase.GetUserPreferencesUseCase
import com.prathamchikitse.domain.usecase.UpdateLanguageUseCase
import com.prathamchikitse.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getUserPreferences: GetUserPreferencesUseCase,
    private val updateLanguage: UpdateLanguageUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun setLanguage(code: String) = viewModelScope.launch { updateLanguage(code) }
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { updateSettings.darkMode(enabled) }
    fun setFontSize(multiplier: Float) = viewModelScope.launch { updateSettings.fontSize(multiplier) }
    fun setTtsEnabled(enabled: Boolean) = viewModelScope.launch { updateSettings.ttsEnabled(enabled) }
    fun setTtsSpeed(rate: Float) = viewModelScope.launch { updateSettings.ttsSpeed(rate) }
    fun setOnboardingCompleted(completed: Boolean) = viewModelScope.launch { updateSettings.onboardingCompleted(completed) }
}
