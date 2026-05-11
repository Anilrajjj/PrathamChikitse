package com.prathamchikitse.data.local

import android.content.Context
import com.prathamchikitse.data.local.entity.EmergencyEntity
import com.prathamchikitse.data.local.entity.HospitalEntity
import com.prathamchikitse.data.local.entity.StepEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parses JSON assets into Room entities.
 * Uses kotlinx.serialization — no reflection overhead.
 */
class JsonAssetParser(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parseEmergencies(): Pair<List<EmergencyEntity>, List<StepEntity>> {
        val raw = context.assets.open("emergencies.json")
            .bufferedReader().use { it.readText() }
        val emergencyList = json.decodeFromString<List<EmergencyJson>>(raw)

        val entities = emergencyList.map { it.toEntity() }
        val steps    = emergencyList.flatMap { e -> e.steps.map { it.toEntity(e.id) } }
        return Pair(entities, steps)
    }

    fun parseHospitals(): List<HospitalEntity> {
        val raw = context.assets.open("hospitals.json")
            .bufferedReader().use { it.readText() }
        return json.decodeFromString<List<HospitalJson>>(raw).map { it.toEntity() }
    }

    // ── JSON models ───────────────────────────────────────────────────────────

    @Serializable
    private data class EmergencyJson(
        val id: String,
        @SerialName("title_en") val titleEn: String,
        @SerialName("title_kn") val titleKn: String,
        @SerialName("description_en") val descriptionEn: String,
        @SerialName("description_kn") val descriptionKn: String,
        val category: String,
        @SerialName("icon_name") val iconName: String,
        @SerialName("keywords_en") val keywordsEn: String = "",
        @SerialName("keywords_kn") val keywordsKn: String = "",
        @SerialName("call_emergency") val callEmergency: Boolean = false,
        val steps: List<StepJson>
    ) {
        fun toEntity() = EmergencyEntity(
            id = id, titleEn = titleEn, titleKn = titleKn,
            descriptionEn = descriptionEn, descriptionKn = descriptionKn,
            category = category, iconName = iconName,
            keywordsEn = keywordsEn, keywordsKn = keywordsKn,
            callEmergency = callEmergency
        )
    }

    @Serializable
    private data class StepJson(
        @SerialName("step_number") val stepNumber: Int,
        @SerialName("title_en") val titleEn: String,
        @SerialName("title_kn") val titleKn: String,
        @SerialName("instruction_en") val instructionEn: String,
        @SerialName("instruction_kn") val instructionKn: String,
        val dos: List<String> = emptyList(),
        val donts: List<String> = emptyList(),
        @SerialName("dos_kn") val dosKn: List<String> = emptyList(),
        @SerialName("donts_kn") val dontsKn: List<String> = emptyList(),
        @SerialName("audio_script_en") val audioScriptEn: String,
        @SerialName("audio_script_kn") val audioScriptKn: String
    ) {
        fun toEntity(emergencyId: String) = StepEntity(
            emergencyId = emergencyId, stepNumber = stepNumber,
            titleEn = titleEn, titleKn = titleKn,
            instructionEn = instructionEn, instructionKn = instructionKn,
            dos = dos, donts = donts, dosKn = dosKn, dontsKn = dontsKn,
            audioScriptEn = audioScriptEn, audioScriptKn = audioScriptKn
        )
    }

    @Serializable
    private data class HospitalJson(
        val id: String,
        val name: String,
        @SerialName("name_kn") val nameKn: String,
        val address: String,
        val district: String,
        val phone: String,
        val lat: Double,
        val lng: Double
    ) {
        fun toEntity() = HospitalEntity(
            id = id, name = name, nameKn = nameKn,
            address = address, district = district,
            phone = phone, lat = lat, lng = lng
        )
    }
}
