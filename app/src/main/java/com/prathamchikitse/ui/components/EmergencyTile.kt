package com.prathamchikitse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathamchikitse.domain.model.Emergency
import com.prathamchikitse.domain.model.EmergencyCategory
import com.prathamchikitse.ui.theme.CriticalRed
import com.prathamchikitse.ui.theme.GeneralTeal
import com.prathamchikitse.ui.theme.ModerateOrange

@Composable
fun EmergencyTile(
    emergency: Emergency,
    language: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(100),
        label = "tile_scale"
    )

    val tileColor = emergency.category.toColor()
    val context = LocalContext.current

    Card(
        modifier = modifier
            .scale(scale)
            .semantics {
                contentDescription = "Emergency: ${if (language == "kn") emergency.titleKn else emergency.titleEn}, Category: ${emergency.category.displayName()}"
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(tileColor.copy(alpha = 0.15f))
            ) {
                val drawableResId = context.resources.getIdentifier(
                    emergency.iconName, "drawable", context.packageName
                )
                if (drawableResId != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .background(tileColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = emergency.toEmergencyIcon(),
                            contentDescription = null,
                            tint = tileColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .background(tileColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = emergency.category.badgeLabel(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = if (language == "kn") emergency.titleKn else emergency.titleEn,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(tileColor, CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = emergency.category.displayName(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = tileColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

fun Emergency.toEmergencyIcon(): ImageVector = Icons.Filled.LocalHospital

internal fun EmergencyCategory.toColor(): Color = when (this) {
    EmergencyCategory.CRITICAL -> CriticalRed
    EmergencyCategory.MODERATE -> ModerateOrange
    EmergencyCategory.GENERAL  -> GeneralTeal
}

private fun EmergencyCategory.badgeLabel(): String = when (this) {
    EmergencyCategory.CRITICAL -> "CRITICAL"
    EmergencyCategory.MODERATE -> "MODERATE"
    EmergencyCategory.GENERAL  -> "GENERAL"
}

private fun EmergencyCategory.displayName(): String = when (this) {
    EmergencyCategory.CRITICAL -> "Critical"
    EmergencyCategory.MODERATE -> "Moderate"
    EmergencyCategory.GENERAL  -> "General"
}
