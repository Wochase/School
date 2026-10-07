package com.example.school.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.school.ui.inbox.InboxNotification
import com.example.school.ui.inbox.InboxViewModel

@Composable
fun StudentInboxScreen(
    studentId: String,
    inboxViewModel: InboxViewModel,
    onNavigateElections: () -> Unit = {},
    onNavigateApply: () -> Unit = {},
    onNavigateInbox: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by inboxViewModel.state.collectAsState()
    var selectedNotification by remember { mutableStateOf<InboxNotification?>(null) }

    LaunchedEffect(inboxViewModel) {
        inboxViewModel.refresh()
    }

    LaunchedEffect(state.notifications) {
        if (selectedNotification == null && state.notifications.isNotEmpty()) {
            selectedNotification = state.notifications.first()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xffdddbd8))
    ) {
        // Main Content Area matching Figma Inbox UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 260.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Inbox",
                    color = Color.Black,
                    style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Panel: Message List Summary Pane
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(15.dp))
                            .background(color = Color(0xffd0d0d0))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header banner
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                color = Color(0xfff08d5e)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${state.unreadCount} unread", color = Color.Black, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                                    Text("${state.notifications.size} messages", color = Color.Black, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                                }
                            }

                            if (state.notifications.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No messages in your inbox", color = Color.DarkGray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.notifications) { notification ->
                                        val isSelected = selectedNotification?.id == notification.id
                                        val isUnread = notification.id !in state.readNotificationIds
                                        Surface(
                                            onClick = {
                                                selectedNotification = notification
                                                inboxViewModel.markRead(notification.id)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xfff0efa7) else if (isUnread) Color.White else Color(0xfff8f8f8)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                                Text(
                                                    text = notification.title,
                                                    style = TextStyle(fontSize = 16.sp, fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal),
                                                    color = Color.Black,
                                                    maxLines = 1
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = notification.timestamp,
                                                    style = TextStyle(fontSize = 12.sp),
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Panel: Mail Content Detail Pane
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(15.dp))
                            .background(color = Color(0xffd0d0d0))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top App Bar Header Banner
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                color = Color(0xfff08d5e)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = selectedNotification?.title ?: "Mail Title",
                                        color = Color.Black,
                                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                                    .padding(24.dp)
                            ) {
                                val notif = selectedNotification
                                if (notif != null) {
                                    Column {
                                        Text(notif.timestamp, style = TextStyle(fontSize = 14.sp), color = Color(0xfff08d5e))
                                        Spacer(Modifier.height(16.dp))
                                        HorizontalDivider(color = Color.LightGray)
                                        Spacer(Modifier.height(16.dp))
                                        Text(notif.message, style = TextStyle(fontSize = 16.sp), color = Color.DarkGray)
                                    }
                                } else {
                                    Text(
                                        text = "Select a message from your list to review official notices.",
                                        color = Color.DarkGray,
                                        style = TextStyle(fontSize = 16.sp),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sidebar Navigation Panel
        StudentSidebar(
            activeTab = "Inbox",
            onNavigateElections = onNavigateElections,
            onNavigateApply = onNavigateApply,
            onNavigateInbox = onNavigateInbox,
            onLogout = onLogout
        )
    }
}
