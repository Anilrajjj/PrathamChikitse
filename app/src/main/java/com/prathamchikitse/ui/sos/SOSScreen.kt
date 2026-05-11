package com.prathamchikitse.ui.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathamchikitse.ui.theme.CriticalRed
import com.prathamchikitse.ui.theme.SOSRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sos_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "ring_alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF3D0000))))
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Spacer(Modifier.weight(1f))

        Text("🚨 EMERGENCY", style = MaterialTheme.typography.headlineMedium.copy(
            color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
        Spacer(Modifier.height(8.dp))
        Text("ತುರ್ತು ಸಹಾಯ / Emergency Help",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)))

        Spacer(Modifier.weight(0.5f))

        // Pulsing SOS button
        Box(contentAlignment = Alignment.Center) {
            // Outer ring animation
            Box(modifier = Modifier.size(220.dp)
                .border(3.dp, SOSRed.copy(alpha = ringAlpha), CircleShape)
                .background(Color.Transparent, CircleShape))
            Box(modifier = Modifier.size(180.dp)
                .border(2.dp, SOSRed.copy(alpha = ringAlpha * 0.5f), CircleShape)
                .background(Color.Transparent, CircleShape))

                // Main SOS button
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                        if (dialIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(dialIntent)
                        }
                    },
                modifier = Modifier.size(150.dp).scale(pulse),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SOSRed),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Phone, null, modifier = Modifier.size(40.dp), tint = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text("112", style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Tap to call National Emergency", style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center))

        Spacer(Modifier.weight(0.5f))

        // Quick call cards
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("Ambulance", "108", Icons.Filled.LocalHospital),
                Triple("Police", "100", Icons.Filled.Shield),
                Triple("Fire", "101", Icons.Filled.Whatshot)
            ).forEach { (label, number, icon) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(label, style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White, fontWeight = FontWeight.Bold))
                                Text("Emergency Helpline", style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f)))
                            }
                        }
                        FilledTonalButton(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = CriticalRed)
                        ) {
                            Text(number, style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}
