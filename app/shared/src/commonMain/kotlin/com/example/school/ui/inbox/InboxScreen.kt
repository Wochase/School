package com.example.e_voting.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.e_voting.ui.*

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    isAdmin: Boolean,
) {
    val state by viewModel.state.collectAsState()
    val notifications = state.notifications
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier.fillMaxSize().background(BackgroundOffWhite).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.widthIn(min = 300.dp, max = 400.dp).fillMaxHeight()) {
            Column {
                Column(Modifier.fillMaxWidth().background(SecondaryNavy).padding(16.dp)) {
                    Text(if (isAdmin) "ADMIN INBOX" else "STUDENT INBOX", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${state.unreadCount} unread · ${notifications.size} messages", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                }
                if (notifications.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(state.error ?: "Your inbox is empty.", color = TextMuted)
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(notifications) { index, notification ->
                            val unread = notification.id !in state.readNotificationIds
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .background(if (selectedIndex == index) PrimaryOrange.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable {
                                        selectedIndex = index
                                        viewModel.markRead(notification.id)
                                    }
                                    .padding(14.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (unread) {
                                        Surface(color = PrimaryOrange, shape = RoundedCornerShape(5.dp), modifier = Modifier.size(8.dp)) {}
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text(
                                        notification.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                                        color = SecondaryNavy,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(notification.message, maxLines = 2, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Spacer(Modifier.height(4.dp))
                                Text(notification.timestamp, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("New application and election updates will appear here.", color = TextMuted)
                }
            } else {
                val notification = notifications[selectedIndex.coerceIn(notifications.indices)]
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text(notification.timestamp, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Text(notification.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    Text(notification.message, style = MaterialTheme.typography.bodyLarge, color = TextDark)
                    if (notification.approved) {
                        Spacer(Modifier.height(20.dp))
                        Surface(color = StatusGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                            Text("Approved", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = StatusGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
