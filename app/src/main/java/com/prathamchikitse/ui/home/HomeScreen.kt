package com.prathamchikitse.ui.home

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prathamchikitse.R
import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.EmergencyCategory
import com.prathamchikitse.ui.components.EmergencyTile
import com.prathamchikitse.ui.theme.CriticalRed
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.ui.theme.SOSRed
import com.prathamchikitse.ui.components.toColor
import com.prathamchikitse.ui.components.toEmergencyIcon
import com.prathamchikitse.viewmodel.HomeViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    onEmergencyClick: (String) -> Unit,
    onHospitalsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSOSClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query   by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isWideScreen = screenWidth >= 600.dp

    var isSearchExpanded by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(0f) }
    var isLongPressing by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isLongPressing) 0.96f else 1f,
        label = "banner_scale"
    )

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            viewModel.onSearchQueryChanged(spokenText)
            isSearchExpanded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (uiState.currentLanguage == "kn") "ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ" else "Pratham Chikitse",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = PrimaryRed, fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (uiState.currentLanguage == "kn") "First Aid Guide" else "First Aid Emergency Guide",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(
                    onClick = onHospitalsClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.LocalHospital, "Hospitals", tint = PrimaryRed)
                }
                FilledTonalIconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Settings, "Settings")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isLongPressing = true
                            tryAwaitRelease()
                            isLongPressing = false
                        },
                        onLongPress = {
                            val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:112"))
                            if (dialIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(dialIntent)
                            }
                        }
                    )
                }
                .background(
                    Brush.horizontalGradient(listOf(SOSRed, CriticalRed)),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (uiState.currentLanguage == "kn") "🚨 ತುರ್ತು?" else "🚨 Emergency?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        if (uiState.currentLanguage == "kn") "112 ಕ್ಕೆ ಕರೆ ಮಾಡಿ" else "Call 112 immediately",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                    if (isLongPressing) {
                        Text(
                            if (uiState.currentLanguage == "kn") "ಡಯಲ್ ಮಾಡಲಾಗುತ್ತಿದೆ..." else "Dialing...",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                }
                FilledTonalIconButton(
                    onClick = onSOSClick,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Phone, "SOS", tint = SOSRed)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(
            visible = uiState.recentlyViewed.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Text(
                    if (uiState.currentLanguage == "kn") "ಇತ್ತೀಚೆಗೆ ವೀಕ್ಷಿಸಲಾಗಿದೆ" else "Recently Viewed",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.recentlyViewed) { emergency ->
                        RecentlyViewedChip(
                            emergency = emergency,
                            language = uiState.currentLanguage,
                            onClick = { onEmergencyClick(emergency.id) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            BasicTextField(
                value = query,
                onValueChange = {
                    viewModel.onSearchQueryChanged(it)
                    isSearchExpanded = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxWidth().wrapContentHeight(Alignment.CenterVertically)) {
                        if (query.isEmpty()) {
                            Text(
                                text = if (uiState.currentLanguage == "kn") "ತುರ್ತು ಹುಡುಕಿ..." else "Search emergencies...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.onSearchQueryChanged("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak emergency name")
                    }
                    voiceSearchLauncher.launch(intent)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Filled.Mic, "Voice Search", tint = PrimaryRed)
            }
        }

        AnimatedVisibility(
            visible = isSearchExpanded && query.isNotEmpty() && uiState.recentSearches.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(uiState.recentSearches.take(5)) { recentQuery ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            viewModel.onSearchQueryChanged(recentQuery)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.History, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                recentQuery,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { viewModel.onCategorySelected(null) },
                label = { Text(if (uiState.currentLanguage == "kn") "ಎಲ್ಲಾ" else "All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryRed.copy(alpha = 0.2f),
                    selectedLabelColor = PrimaryRed
                )
            )
            EmergencyCategory.entries.forEach { cat ->
                val selected = uiState.selectedCategory == cat
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onCategorySelected(cat) },
                    label = { Text(cat.displayName(uiState.currentLanguage)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = cat.chipColor().copy(alpha = 0.2f),
                        selectedLabelColor = cat.chipColor()
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("Error loading emergencies", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.error ?: "Unknown error", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (uiState.emergencies.isEmpty() && query.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text(
                        "🔍",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (uiState.currentLanguage == "kn") "'$query' ಗೆ ಯಾವುದೇ ತುರ್ತು ಪರಿಸ್ಥಿತಿಗಳು ಕಂಡುಬಂದಿಲ್ಲ" else "No emergencies found for '$query'",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        } else {
            val columns = if (isWideScreen) 3 else 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.emergencies, key = { it.id }) { emergency ->
                    EmergencyTile(
                        emergency = emergency,
                        language = uiState.currentLanguage,
                        onClick = {
                            viewModel.onEmergencyViewed(emergency)
                            onEmergencyClick(emergency.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyViewedChip(
    emergency: Emergency,
    language: String,
    onClick: () -> Unit
) {
    val tileColor = emergency.category.toColor()
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tileColor.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(tileColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = emergency.toEmergencyIcon(),
                    contentDescription = null,
                    tint = tileColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (language == "kn") emergency.titleKn else emergency.titleEn,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(tileColor, CircleShape)
            )
        }
    }
}

private fun EmergencyCategory.chipColor() = when (this) {
    EmergencyCategory.CRITICAL -> CriticalRed
    EmergencyCategory.MODERATE -> com.prathamchikitse.ui.theme.ModerateOrange
    EmergencyCategory.GENERAL  -> com.prathamchikitse.ui.theme.GeneralTeal
}

private fun EmergencyCategory.displayName(language: String) = when (this) {
    EmergencyCategory.CRITICAL -> if (language == "kn") "ಗಂಭೀರ" else "Critical"
    EmergencyCategory.MODERATE -> if (language == "kn") "ಮಧ್ಯಮ" else "Moderate"
    EmergencyCategory.GENERAL  -> if (language == "kn") "ಸಾಮಾನ್ಯ" else "General"
}
