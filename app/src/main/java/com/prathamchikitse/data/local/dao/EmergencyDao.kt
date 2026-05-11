package com.prathamchikitse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.prathamchikitse.data.local.entity.EmergencyEntity
import com.prathamchikitse.data.local.entity.EmergencyWithSteps
import com.prathamchikitse.data.local.entity.HospitalEntity
import com.prathamchikitse.data.local.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {

    // ── Emergency queries ─────────────────────────────────────────────────────
    @Query("SELECT * FROM emergencies ORDER BY category ASC")
    fun getAllEmergencies(): Flow<List<EmergencyEntity>>

    @Query("SELECT * FROM emergencies WHERE id = :id")
    suspend fun getEmergencyById(id: String): EmergencyEntity?

    @Transaction
    @Query("SELECT * FROM emergencies WHERE id = :id")
    suspend fun getEmergencyWithSteps(id: String): EmergencyWithSteps?

    @Transaction
    @Query("SELECT * FROM emergencies ORDER BY category ASC")
    fun getAllEmergenciesWithSteps(): Flow<List<EmergencyWithSteps>>

    @Query("""
        SELECT * FROM emergencies
        WHERE titleEn LIKE '%' || :query || '%'
        OR titleKn LIKE '%' || :query || '%'
        OR descriptionEn LIKE '%' || :query || '%'
        OR descriptionKn LIKE '%' || :query || '%'
        OR keywordsEn LIKE '%' || :query || '%'
        OR keywordsKn LIKE '%' || :query || '%'
    """)
    fun searchEmergencies(query: String): Flow<List<EmergencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencies(emergencies: List<EmergencyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("SELECT COUNT(*) FROM emergencies")
    suspend fun getCount(): Int

    @Query("SELECT * FROM steps WHERE emergencyId = :emergencyId ORDER BY stepNumber ASC")
    suspend fun getStepsForEmergency(emergencyId: String): List<StepEntity>

    // ── Hospital queries ──────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitals(hospitals: List<HospitalEntity>)

    @Query("SELECT * FROM hospitals WHERE district = :district")
    suspend fun getHospitalsByDistrict(district: String): List<HospitalEntity>

    @Query("SELECT * FROM hospitals")
    suspend fun getAllHospitals(): List<HospitalEntity>
}
