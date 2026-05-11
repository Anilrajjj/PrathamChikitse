package com.prathamchikitse.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.prathamchikitse.domain.model.NearbyHospital
import com.prathamchikitse.domain.usecase.GetNearestHospitalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.pow
import javax.inject.Inject

sealed class LocationState {
    object Unknown : LocationState()
    object PermissionDenied : LocationState()
    object GpsDisabled : LocationState()
    object Loading : LocationState()
    data class Available(val lat: Double, val lng: Double) : LocationState()
}

data class HospitalUiState(
    val hospitals: List<NearbyHospital> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val locationState: LocationState = LocationState.Unknown,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class HospitalViewModel @Inject constructor(
    private val getNearestHospitals: GetNearestHospitalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HospitalUiState())
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun checkAndFetchLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            val permissionGranted = isLocationPermissionGranted(context)
            if (!permissionGranted) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        locationState = LocationState.PermissionDenied,
                        error = "Location permission is required to find nearby hospitals"
                    )
                }
                return@launch
            }

            if (!isGpsEnabled(context)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        locationState = LocationState.GpsDisabled,
                        error = "Please enable location/GPS to find nearby hospitals"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(locationState = LocationState.Loading) }

            val location = getCurrentLocation(context)
            if (location != null) {
                _uiState.update { it.copy(locationState = LocationState.Available(location.latitude, location.longitude)) }
                loadHospitalsFallback(location.latitude, location.longitude)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        locationState = LocationState.GpsDisabled,
                        error = "Could not get your location. Showing default hospitals."
                    )
                }
                loadHospitalsFallback(12.9716, 77.5946)
            }
        }
    }

    private fun loadHospitalsFallback(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val hospitals = getNearestHospitals(userLat = lat, userLng = lng, limit = 20)
                _uiState.update { it.copy(hospitals = hospitals, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load hospitals") }
            }
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2).pow(2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).pow(2)
        return r * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }

    fun onGpsEnabled(context: Context) {
        checkAndFetchLocation(context)
    }

    private fun isLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? = withTimeoutOrNull(10_000) {
        fusedLocationClient.lastLocation.await()
    }

    private fun loadHospitals(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val hospitals = getNearestHospitals(userLat = lat, userLng = lng, limit = 20)
                _uiState.update { it.copy(hospitals = hospitals, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load hospitals") }
            }
        }
    }
}
