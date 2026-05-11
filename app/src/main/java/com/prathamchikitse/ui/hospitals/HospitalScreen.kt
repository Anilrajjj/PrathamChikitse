package com.prathamchikitse.ui.hospitals

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prathamchikitse.domain.model.NearbyHospital
import com.prathamchikitse.ui.theme.GeneralTeal
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.viewmodel.HospitalViewModel
import com.prathamchikitse.viewmodel.LocationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalScreen(onBack: () -> Unit, viewModel: HospitalViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var permissionRequested by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionRequested = true
            val granted = permissions.values.any { it }
            if (granted) {
                viewModel.checkAndFetchLocation(context)
            }
        }
    )

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted) {
            viewModel.checkAndFetchLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text("Nearby Hospitals", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
            },
            actions = {
                IconButton(onClick = { viewModel.checkAndFetchLocation(context) }) {
                    Icon(Icons.Filled.Refresh, "Refresh location")
                }
            }
        )

        when (uiState.locationState) {
            LocationState.PermissionDenied -> {
                PermissionDeniedCard(
                    onRetry = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
            LocationState.GpsDisabled -> {
                GpsDisabledCard(
                    onEnableLocation = {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }
            LocationState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryRed)
                        Spacer(Modifier.height(16.dp))
                        Text("Getting your location...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            is LocationState.Available -> {
                InfoBanner()
                HospitalList(uiState = uiState, context = context, viewModel = viewModel)
            }
            LocationState.Unknown -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedCard(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.LocationOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Location Permission Denied", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Please grant location permission to find nearby hospitals near you.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)) {
            Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Grant Permission")
        }
    }
}

@Composable
private fun GpsDisabledCard(onEnableLocation: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.MyLocation, null, tint = GeneralTeal, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Turn On Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Please enable GPS/Location services to find hospitals near your current position.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onEnableLocation, colors = ButtonDefaults.buttonColors(containerColor = GeneralTeal)) {
            Icon(Icons.Filled.Settings, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Enable Location")
        }
    }
}

@Composable
private fun InfoBanner() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .background(GeneralTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Place, null, tint = GeneralTeal, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Showing hospitals sorted by distance from your location",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)))
    }
}

@Composable
private fun HospitalList(
    uiState: com.prathamchikitse.viewmodel.HospitalUiState,
    context: android.content.Context,
    viewModel: HospitalViewModel
) {
    if (uiState.isLoading && uiState.hospitals.isNotEmpty()) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            LinearProgressIndicator(color = PrimaryRed, modifier = Modifier.fillMaxWidth())
        }
    }

    if (uiState.error != null && uiState.hospitals.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text("Error loading hospitals", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text(uiState.error ?: "Unknown error", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    if (uiState.hospitals.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.LocationOff, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No hospitals found nearby", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(uiState.hospitals) { hospital ->
            HospitalCard(
                hospital = hospital,
                onCallClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.phone}"))
                    if (dialIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(dialIntent)
                    }
                },
                onDirectionsClick = {
                    val gmmIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:${hospital.lat},${hospital.lng}?q=${Uri.encode(hospital.name)}")
                        setPackage("com.google.android.apps.maps")
                    }
                    if (gmmIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(gmmIntent)
                    } else {
                        val fallbackIntent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://maps.google.com/?q=${hospital.lat},${hospital.lng}"))
                        context.startActivity(fallbackIntent)
                    }
                }
            )
        }
    }
}

@Composable
private fun HospitalCard(hospital: NearbyHospital, onCallClick: () -> Unit, onDirectionsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).background(PrimaryRed.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.LocalHospital, null, tint = PrimaryRed, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hospital.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(hospital.address, style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)), maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, null, tint = GeneralTeal, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("%.1f km".format(hospital.distanceKm),
                        style = MaterialTheme.typography.labelMedium.copy(color = GeneralTeal))
                    Spacer(Modifier.width(8.dp))
                    Text(hospital.district, style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = onCallClick, colors = ButtonDefaults.buttonColors(containerColor = GeneralTeal),
                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Filled.Phone, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call")
                }
                OutlinedButton(onClick = onDirectionsClick,
                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Filled.Place, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Dir")
                }
            }
        }
    }
}
