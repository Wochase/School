package com.example.school.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.school.ui.*

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    isAdmin: Boolean,
) {
    val state by viewModel.state.collectAsState()
    var selectedNotification by remember { mutableStateOf<InboxNotification?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.refresh()
    }

    LaunchedEffect(state.notifications) {
        if (selectedNotification == null && state.notifications.isNotEmpty()) {
            selectedNotification = state.notifications.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAdmin) "Admin Inbox & Notifications" else "Student Inbox",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundOffWhite)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Panel: Notification List Summary
            Card(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryOrange
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${state.unreadCount} unread", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${state.notifications.size} messages", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (state.notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No messages in your inbox", color = TextMuted)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.notifications) { notification ->
                                val isSelected = selectedNotification?.id == notification.id
                                val isUnread = notification.id !in state.readNotificationIds
                                Surface(
                                    onClick = {
                                        selectedNotification = notification
                                        viewModel.markRead(notification.id)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) PrimaryOrange.copy(alpha = 0.12f) else if (isUnread) Color(0xFFEFF6FF) else Color.Transparent
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = notification.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                                                color = TextDark,
                                                maxLines = 1
                                            )
                                            if (isUnread) {
                                                Surface(
                                                    color = PrimaryOrange,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Box(modifier = Modifier.size(8.dp))
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = notification.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Panel: Message Content View
            Card(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    val notif = selectedNotification
                    if (notif != null) {
                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = notif.timestamp,
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryOrange
                        )
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextDark
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select a message from your list to review official notices.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
