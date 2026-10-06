package com.example.e_voting.ui.admin

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
import androidx.compose.ui.unit.sp
import com.example.e_voting.data.model.AuditLogEntry
import com.example.e_voting.network.ElectionRepository
import com.example.e_voting.ui.*

@Composable
fun AuditLogsScreen(
    repository: ElectionRepository,
) {
    var filterCategory by remember { mutableStateOf("All") }
    val logs by repository.auditLogs.collectAsState()
    val filteredLogs = logs.asReversed().filter {
        when (filterCategory) {
            "Admin actions" -> it.actorRole == "Admin" || it.actorRole.contains("Administrator")
            "Student actions" -> it.actorRole == "Student"
            else -> true
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(BackgroundOffWhite).padding(16.dp),
        ) {
            Text("Live audit activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
            Text("New application, approval, and election-stage events appear here as they happen.", color = TextMuted)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Admin actions", "Student actions").forEach { category ->
                    FilterChip(
                        selected = filterCategory == category,
                        onClick = { filterCategory = category },
                        label = { Text(category) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (filteredLogs.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No audit events yet.", fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
                        Spacer(Modifier.height(4.dp))
                        Text("Real activity will be listed here. No sample records are displayed.", color = TextMuted)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredLogs) { log ->
                        AuditLogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: AuditLogEntry) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("#${log.logId} · ${log.timestamp}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                LogStatusBadge(log.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(log.action, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(4.dp))
            Text("${log.actorRole} · ${log.actorId}", style = MaterialTheme.typography.bodySmall, color = SecondaryNavy)
        }
    }
}

@Composable
fun LogStatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Success" -> StatusGreen.copy(alpha = 0.15f) to StatusGreen
        "Warning" -> StatusAmber.copy(alpha = 0.15f) to StatusAmber
        else -> Color.Gray.copy(alpha = 0.15f) to Color.DarkGray
    }
    Surface(color = bgColor, shape = RoundedCornerShape(6.dp)) {
        Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = textColor)
    }
}
