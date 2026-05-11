package com.prathamchikitse.domain.repository

import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.NearbyHospital
import com.prathamchikitse.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface EmergencyRepository {
    fun getAllEmergencies(): Flow<List<Emergency>>
    fun searchEmergencies(query: String): Flow<List<Emergency>>
    suspend fun getEmergencyById(id: String): Emergency?
    suspend fun getAllHospitals(): List<NearbyHospital>
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateLanguage(code: String)
    suspend fun updateDarkMode(enabled: Boolean)
    suspend fun updateFontSize(multiplier: Float)
    suspend fun updateTtsEnabled(enabled: Boolean)
    suspend fun updateTtsSpeed(rate: Float)
    suspend fun updateOnboardingCompleted(completed: Boolean)
}
