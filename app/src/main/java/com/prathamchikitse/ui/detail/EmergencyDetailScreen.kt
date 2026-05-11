package com.prathamchikitse.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prathamchikitse.domain.model.EmergencyStep
import com.prathamchikitse.tts.TtsStatus
import com.prathamchikitse.ui.theme.DosGreen
import com.prathamchikitse.ui.theme.DontsRed
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.viewmodel.EmergencyDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyDetailScreen(
    emergencyId: String,
    onBack: () -> Unit,
    onHospitalsClick: () -> Unit,
    onChatClick: () -> Unit,
    viewModel: EmergencyDetailViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()
    val emergency = uiState.emergency

    if (uiState.isLoading || emergency == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryRed)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { emergency.steps.size })
    val coroutineScope = rememberCoroutineScope()

    // Sync pager ↔ ViewModel step index
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.goToStep(page)
        }
    }
    LaunchedEffect(uiState.currentStepIndex) {
        if (pagerState.currentPage != uiState.currentStepIndex) {
            pagerState.animateScrollToPage(uiState.currentStepIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top app bar ───────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Text(
                    if (uiState.currentLanguage == "kn") emergency.titleKn else emergency.titleEn,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                // Language toggle
                OutlinedButton(
                    onClick = {
                        viewModel.setLanguage(if (uiState.currentLanguage == "en") "kn" else "en")
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (uiState.currentLanguage == "en") "ಕನ್ನಡ" else "EN",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // ── TTS Progress Bar ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = ttsState.status == TtsStatus.PLAYING || ttsState.status == TtsStatus.PAUSED
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { ttsState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = DosGreen,
                    trackColor = DosGreen.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reading step ${uiState.currentStepIndex + 1}...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DosGreen
                        )
                    )
                    Text(
                        text = "${(ttsState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        // ── Step counter ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step ${uiState.currentStepIndex + 1} of ${emergency.steps.size}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            // Step dots indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(emergency.steps.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == uiState.currentStepIndex) 10.dp else 7.dp)
                            .background(
                                if (index == uiState.currentStepIndex) PrimaryRed
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                CircleShape
                            )
                    )
                }
            }
        }

        // ── Horizontal pager (steps) ──────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            AnimatedContent(
                targetState = pageIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "step_transition"
            ) { index ->
                emergency.steps.getOrNull(index)?.let { step ->
                    StepCard(
                        step = step,
                        language = uiState.currentLanguage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ── Navigation + TTS controls ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // TTS big button
            TTSButton(
                status = ttsState.status,
                onToggle = viewModel::toggleTts,
                onStop = viewModel::stopTts,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Prev / Next navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = viewModel::prevStep,
                    enabled = uiState.currentStepIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (uiState.currentLanguage == "kn") "ಹಿಂದೆ" else "Previous")
                }
                Button(
                    onClick = {
                        if (uiState.currentStepIndex < emergency.steps.size - 1)
                            viewModel.nextStep()
                        else
                            onHospitalsClick()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text(
                        if (uiState.currentStepIndex < emergency.steps.size - 1) {
                            if (uiState.currentLanguage == "kn") "ಮುಂದೆ" else "Next"
                        } else {
                            "Hospitals"
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (uiState.currentStepIndex < emergency.steps.size - 1)
                            Icons.AutoMirrored.Filled.ArrowForward
                        else
                            Icons.Filled.LocalHospital,
                        null
                    )
                }
            }
        }
    }
}

@Composable
private fun TTSButton(
    status: TtsStatus,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tts_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onToggle,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .then(if (status == TtsStatus.PLAYING) Modifier.scale(pulse) else Modifier),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (status) {
                    TtsStatus.PLAYING -> DosGreen
                    TtsStatus.PAUSED  -> PrimaryRed.copy(alpha = 0.8f)
                    else              -> PrimaryRed
                }
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = when (status) {
                    TtsStatus.PLAYING -> Icons.Filled.Pause
                    else              -> Icons.Filled.PlayArrow
                },
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = when (status) {
                    TtsStatus.PLAYING -> "Pause Audio"
                    TtsStatus.PAUSED  -> "Resume Audio"
                    else              -> "🔊 Play Audio"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (status == TtsStatus.PLAYING || status == TtsStatus.PAUSED) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(Icons.Filled.Stop, "Stop", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StepCard(
    step: EmergencyStep,
    language: String,
    modifier: Modifier = Modifier
) {
    val isKannada = language == "kn"
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(PrimaryRed.copy(alpha = 0.12f), CircleShape)
                .border(2.dp, PrimaryRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${step.stepNumber}",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = PrimaryRed,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Step title
        Text(
            text = if (isKannada) step.titleKn else step.titleEn,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // Instruction
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isKannada) step.instructionKn else step.instructionEn,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(16.dp)
            )
        }

        // Do's
        val dos = if (isKannada) step.dosKn else step.dos
        if (dos.isNotEmpty()) {
            DosDontsCard(
                title = if (isKannada) "✅ ಮಾಡಬೇಕಾದದ್ದು" else "✅ Do's",
                items = dos,
                color = DosGreen
            )
        }

        // Don'ts
        val donts = if (isKannada) step.dontsKn else step.donts
        if (donts.isNotEmpty()) {
            DosDontsCard(
                title = if (isKannada) "❌ ಮಾಡಬಾರದ್ದು" else "❌ Don'ts",
                items = donts,
                color = DontsRed
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DosDontsCard(
    title: String,
    items: List<String>,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = color, fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(8.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (color == DosGreen) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
