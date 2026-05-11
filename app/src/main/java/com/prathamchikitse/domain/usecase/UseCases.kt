package com.prathamchikitse.domain.usecase

import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.NearbyHospital
import com.prathamchikitse.domain.model.UserPreferences
import com.prathamchikitse.domain.repository.EmergencyRepository
import com.prathamchikitse.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.*

// ─── Get all emergencies ───────────────────────────────────────────────────────
class GetAllEmergenciesUseCase @Inject constructor(
    private val repository: EmergencyRepository
) {
    operator fun invoke(): Flow<List<Emergency>> = repository.getAllEmergencies()
}

// ─── Search emergencies ────────────────────────────────────────────────────────
class SearchEmergenciesUseCase @Inject constructor(
    private val repository: EmergencyRepository
) {
    operator fun invoke(query: String): Flow<List<Emergency>> =
        if (query.isBlank()) repository.getAllEmergencies()
        else repository.searchEmergencies(query.trim())
}

// ─── Get emergency detail ──────────────────────────────────────────────────────
class GetEmergencyDetailUseCase @Inject constructor(
    private val repository: EmergencyRepository
) {
    suspend operator fun invoke(id: String): Emergency? =
        repository.getEmergencyById(id)
}

// ─── Get nearest hospitals ────────────────────────────────────────────────────
class GetNearestHospitalsUseCase @Inject constructor(
    private val repository: EmergencyRepository
) {
    /**
     * Returns the [limit] nearest hospitals to [userLat]/[userLng].
     * Uses Haversine formula for great-circle distance.
     * If location is unavailable, defaults to Bengaluru centre.
     */
    suspend operator fun invoke(
        userLat: Double = 12.9716,
        userLng: Double = 77.5946,
        limit: Int = 5
    ): List<NearbyHospital> {
        return repository.getAllHospitals()
            .map { hospital ->
                hospital.distanceKm = haversineKm(userLat, userLng, hospital.lat, hospital.lng)
                hospital
            }
            .sortedBy { it.distanceKm }
            .take(limit)
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

// ─── User preferences ─────────────────────────────────────────────────────────
class GetUserPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> = repository.getUserPreferences()
}

class UpdateLanguageUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(code: String) = repository.updateLanguage(code)
}

class UpdateSettingsUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend fun darkMode(enabled: Boolean) = repository.updateDarkMode(enabled)
    suspend fun fontSize(multiplier: Float) = repository.updateFontSize(multiplier)
    suspend fun ttsEnabled(enabled: Boolean) = repository.updateTtsEnabled(enabled)
    suspend fun ttsSpeed(rate: Float) = repository.updateTtsSpeed(rate)
    suspend fun onboardingCompleted(completed: Boolean) = repository.updateOnboardingCompleted(completed)
}
