package com.prathamchikitse.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.ui.theme.SOSRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onShowOnboarding: () -> Unit, onSplashComplete: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.7f) }
    var shouldNavigate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(600, easing = EaseOutCubic))
        scale.animateTo(1f, tween(500, easing = EaseOutCubic))
        delay(800)
        shouldNavigate = true
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            val prefs = context.getSharedPreferences("pratham_prefs_private", android.content.Context.MODE_PRIVATE)
            val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
            if (hasCompletedOnboarding) {
                onSplashComplete()
            } else {
                onShowOnboarding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, SOSRed.copy(alpha = 0.08f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(PrimaryRed.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalHospital,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            )
            Text(
                text = "Pratham-Chikitse",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "First Aid Emergency Guide",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            )
        }
    }
}
