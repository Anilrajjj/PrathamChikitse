package com.prathamchikitse.data.repository

import com.prathamchikitse.data.local.dao.EmergencyDao
import com.prathamchikitse.data.local.entity.EmergencyEntity
import com.prathamchikitse.data.local.entity.EmergencyWithSteps
import com.prathamchikitse.data.local.entity.HospitalEntity
import com.prathamchikitse.data.local.entity.StepEntity
import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.EmergencyCategory
import com.prathamchikitse.domain.model.EmergencyStep
import com.prathamchikitse.domain.model.NearbyHospital
import com.prathamchikitse.domain.repository.EmergencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepositoryImpl @Inject constructor(
    private val dao: EmergencyDao
) : EmergencyRepository {

    override fun getAllEmergencies(): Flow<List<Emergency>> =
        dao.getAllEmergenciesWithSteps().map { list ->
            list.map { it.toDomain() }
        }

    override fun searchEmergencies(query: String): Flow<List<Emergency>> =
        dao.searchEmergencies(query).map { list ->
            list.map { entity ->
                val steps = dao.getStepsForEmergency(entity.id)
                    .sortedBy { it.stepNumber }
                    .map { it.toDomain() }
                Emergency(
                    id = entity.id,
                    titleEn = entity.titleEn,
                    titleKn = entity.titleKn,
                    descriptionEn = entity.descriptionEn,
                    descriptionKn = entity.descriptionKn,
                    category = EmergencyCategory.valueOf(entity.category),
                    iconName = entity.iconName,
                    keywordsEn = entity.keywordsEn,
                    keywordsKn = entity.keywordsKn,
                    callEmergency = entity.callEmergency,
                    steps = steps
                )
            }
        }

    override suspend fun getEmergencyById(id: String): Emergency? =
        dao.getEmergencyWithSteps(id)?.toDomain()

    override suspend fun getAllHospitals(): List<NearbyHospital> =
        dao.getAllHospitals().map { it.toDomain() }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun EmergencyWithSteps.toDomain(): Emergency = Emergency(
        id = emergency.id,
        titleEn = emergency.titleEn,
        titleKn = emergency.titleKn,
        descriptionEn = emergency.descriptionEn,
        descriptionKn = emergency.descriptionKn,
        category = EmergencyCategory.valueOf(emergency.category),
        iconName = emergency.iconName,
        keywordsEn = emergency.keywordsEn,
        keywordsKn = emergency.keywordsKn,
        callEmergency = emergency.callEmergency,
        steps = steps.sortedBy { it.stepNumber }.map { it.toDomain() }
    )

    private fun StepEntity.toDomain(): EmergencyStep = EmergencyStep(
        emergencyId = emergencyId,
        stepNumber = stepNumber,
        titleEn = titleEn,
        titleKn = titleKn,
        instructionEn = instructionEn,
        instructionKn = instructionKn,
        dos = dos,
        donts = donts,
        dosKn = dosKn,
        dontsKn = dontsKn,
        audioScriptEn = audioScriptEn,
        audioScriptKn = audioScriptKn
    )

    private fun HospitalEntity.toDomain(): NearbyHospital = NearbyHospital(
        id = id, name = name, nameKn = nameKn,
        address = address, district = district,
        phone = phone, lat = lat, lng = lng
    )
}
