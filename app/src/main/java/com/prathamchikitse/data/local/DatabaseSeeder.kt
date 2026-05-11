package com.prathamchikitse.data.local

import com.prathamchikitse.data.local.dao.EmergencyDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val dao: EmergencyDao,
    private val parser: JsonAssetParser
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedIfNeeded() {
        scope.launch {
            try {
                if (dao.getCount() == 0) {
                    val (emergencies, steps) = parser.parseEmergencies()
                    val hospitals = parser.parseHospitals()
                    dao.insertEmergencies(emergencies)
                    dao.insertSteps(steps)
                    dao.insertHospitals(hospitals)
                }
            } catch (e: Exception) {
                android.util.Log.e("DatabaseSeeder", "Seed failed", e)
            }
        }
    }
}
