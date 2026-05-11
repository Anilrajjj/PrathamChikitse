package com.prathamchikitse.domain.model

/**
 * Pure domain models — no Room/framework dependencies.
 * Used throughout domain layer and mapped to/from data entities.
 */

enum class EmergencyCategory(val colorHex: String) {
    CRITICAL("#C62828"),
    MODERATE("#E65100"),
    GENERAL("#00695C")
}

data class Emergency(
    val id: String,
    val titleEn: String,
    val titleKn: String,
    val descriptionEn: String,
    val descriptionKn: String,
    val category: EmergencyCategory,
    val iconName: String,
    val keywordsEn: String = "",
    val keywordsKn: String = "",
    val callEmergency: Boolean,
    val steps: List<EmergencyStep>
)

data class EmergencyStep(
    val emergencyId: String,
    val stepNumber: Int,
    val titleEn: String,
    val titleKn: String,
    val instructionEn: String,
    val instructionKn: String,
    val dos: List<String>,          // English dos
    val donts: List<String>,        // English don'ts
    val dosKn: List<String>,        // Kannada dos
    val dontsKn: List<String>,      // Kannada don'ts
    val audioScriptEn: String,
    val audioScriptKn: String
)

data class NearbyHospital(
    val id: String,
    val name: String,
    val nameKn: String,
    val address: String,
    val district: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    var distanceKm: Double = 0.0    // computed at runtime
)

data class UserPreferences(
    val languageCode: String = "en",  // "en", "kn", or "hi"
    val isDarkMode: Boolean = true,
    val fontSizeMultiplier: Float = 1.0f,
    val ttsEnabled: Boolean = true,
    val ttsSpeedRate: Float = 0.85f,
    val hasCompletedOnboarding: Boolean = false
)
