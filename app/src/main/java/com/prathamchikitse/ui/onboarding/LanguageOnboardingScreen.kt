package com.prathamchikitse.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.ui.theme.SOSRed
import com.prathamchikitse.viewmodel.SettingsViewModel

@Composable
fun LanguageOnboardingScreen(
    onComplete: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500, easing = EaseOutCubic))
        scale.animateTo(1f, tween(400, easing = EaseOutCubic))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, SOSRed.copy(alpha = 0.05f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Choose Your Language",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "ನಿಮ್ಮ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ / अपनी भाषा चुनें",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            listOf(
                "en" to "English",
                "kn" to "ಕನ್ನಡ (Kannada)",
                "hi" to "हिन्दी (Hindi)"
            ).forEach { (code, label) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLanguage = code },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguage == code)
                            PrimaryRed.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (selectedLanguage == code) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedLanguage == code) PrimaryRed else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (selectedLanguage == code) {
                            Icon(
                                Icons.Filled.Check,
                                null,
                                tint = PrimaryRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedLanguage?.let { lang ->
                        viewModel.setLanguage(lang)
                        viewModel.setOnboardingCompleted(true)
                        val prefs = context.getSharedPreferences("pratham_prefs_private", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                        onComplete(lang)
                    }
                },
                enabled = selectedLanguage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text(
                    "Get Started",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
