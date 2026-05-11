package com.prathamchikitse.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChange: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            SettingsSection("Language / ಭಾಷೆ") {
                listOf(
                    "en" to "English",
                    "kn" to "ಕನ್ನಡ (Kannada)",
                    "hi" to "हिन्दी (Hindi)"
                ).forEach { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (prefs.languageCode != code) {
                                    viewModel.setLanguage(code)
                                    onLanguageChange(code)
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            fontWeight = if (prefs.languageCode == code) FontWeight.Bold else FontWeight.Normal,
                            color = if (prefs.languageCode == code) PrimaryRed else MaterialTheme.colorScheme.onSurface
                        )
                        if (prefs.languageCode == code) {
                            Icon(
                                Icons.Filled.Check,
                                null,
                                tint = PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            SettingsSection("Appearance") {
                SettingsToggleRow(
                    label = "Dark Mode",
                    icon = Icons.Filled.DarkMode,
                    checked = prefs.isDarkMode,
                    onCheckedChange = viewModel::setDarkMode
                )
            }

            SettingsSection("Audio (TTS)") {
                SettingsToggleRow(
                    label = "Text-to-Speech",
                    icon = Icons.Filled.VolumeUp,
                    checked = prefs.ttsEnabled,
                    onCheckedChange = viewModel::setTtsEnabled
                )
                if (prefs.ttsEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Text("Speech Speed: ${"%.0f".format(prefs.ttsSpeedRate * 100)}%",
                        style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = prefs.ttsSpeedRate,
                        onValueChange = viewModel::setTtsSpeed,
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed)
                    )
                }
            }

            SettingsSection("Text Size") {
                Text("Font Size: ${"%.0f".format(prefs.fontSizeMultiplier * 100)}%",
                    style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = prefs.fontSizeMultiplier,
                    onValueChange = viewModel::setFontSize,
                    valueRange = 0.8f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryRed, activeTrackColor = PrimaryRed)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                    Text("Pratham-Chikitse v2.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text("First-Aid Emergency Offline Guide", style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                    Text("Offline-first • English + Kannada + Hindi", style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                    Text("36+ emergencies • Google Places integration", style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(
            color = PrimaryRed, fontWeight = FontWeight.Bold,
            letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp)
        ))
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryRed, checkedTrackColor = PrimaryRed.copy(alpha = 0.4f)))
    }
}
