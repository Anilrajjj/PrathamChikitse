package com.prathamchikitse.domain.usecase

import com.prathamchikitse.domain.model.NearbyHospital
import com.prathamchikitse.domain.repository.EmergencyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetNearestHospitalsUseCaseTest {

    private lateinit var repository: EmergencyRepository
    private lateinit var useCase: GetNearestHospitalsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetNearestHospitalsUseCase(repository)
    }

    @Test
    fun `invoke returns hospitals sorted by distance`() = runTest {
        val hospitals = listOf(
            NearbyHospital("1", "Hospital A", "A", "Addr1", "Dist1", "111", 12.97, 77.60, 5.0),
            NearbyHospital("2", "Hospital B", "B", "Addr2", "Dist2", "222", 12.97, 77.59, 2.0),
            NearbyHospital("3", "Hospital C", "C", "Addr3", "Dist3", "333", 12.97, 77.61, 8.0)
        )
        coEvery { repository.getAllHospitals() } returns hospitals

        val result = useCase(12.9716, 77.5946, limit = 2)

        assertEquals(2, result.size)
        assertEquals("Hospital B", result[0].name)
        assertEquals("Hospital A", result[1].name)
    }

    @Test
    fun `invoke calculates distance correctly`() = runTest {
        val hospitals = listOf(
            NearbyHospital("1", "Hospital A", "A", "Addr1", "Dist1", "111", 12.9716, 77.5946, 0.0)
        )
        coEvery { repository.getAllHospitals() } returns hospitals

        val result = useCase(12.9716, 77.5946)

        assertEquals(1, result.size)
        assertEquals(0.0, result[0].distanceKm, 0.1)
    }
}
