package com.prathamchikitse.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prathamchikitse.chat.EmergencyChatAssistant
import com.prathamchikitse.ui.theme.PrimaryRed
import com.prathamchikitse.viewmodel.EmergencyDetailViewModel


@Composable
fun EmergencyChatScreen(
    emergencyId: String,
    onBack: () -> Unit,
    viewModel: EmergencyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emergency = uiState.emergency

    val messages = remember {
        mutableStateListOf<EmergencyChatAssistant.ChatMessage>()
    }
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(emergency, uiState.currentLanguage) {
        if (emergency != null && messages.isEmpty()) {
            messages.add(EmergencyChatAssistant.getWelcomeMessage(emergency, uiState.currentLanguage))
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (inputText.isNotBlank() && emergency != null) {
            messages.add(EmergencyChatAssistant.ChatMessage(text = inputText, isUser = true))
            messages.add(EmergencyChatAssistant.getResponse(inputText, uiState.currentLanguage))
            inputText = ""
        }
    }

    if (emergency == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryRed)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                Text("First Aid Chat", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
            Text(
                "Offline Assistant • ${if (uiState.currentLanguage == "kn") emergency.titleKn else emergency.titleEn}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(PrimaryRed.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, null, tint = PrimaryRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "I provide general first aid guidance. For emergencies, always call 112.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your question...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { sendMessage() },
                modifier = Modifier.size(48.dp).background(PrimaryRed, RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.Filled.Send, "Send", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: EmergencyChatAssistant.ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (message.isUser) PrimaryRed else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                if (!message.isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalHospital, null, tint = PrimaryRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Assistant",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
