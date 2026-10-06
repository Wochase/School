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
import com.example.e_voting.ui.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onViewResults: (electionId: String) -> Unit,
    onCreateElection: () -> Unit,
    onSetupElection: (electionId: String) -> Unit,
    onManageCandidates: () -> Unit = {},
    onElectionControl: () -> Unit = {},
    onViewAuditLogs: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
) {
    val adminState by viewModel.adminState.collectAsState()
    var electionToStartEarly by remember { mutableStateOf<com.example.e_voting.data.model.Election?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboard()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundOffWhite),
        ) {
            when (val state = adminState) {
                    is AdminUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryOrange)
                    is AdminUiState.DashboardLoaded -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Admin election stages", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            Text("Publish roles, review applications, then build the ballot from approved candidates.", color = TextMuted)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(onClick = onCreateElection) { Text("Open vetting") }
                                OutlinedButton(onClick = onManageCandidates) { Text("Review candidates") }
                                OutlinedButton(onClick = onElectionControl) { Text("Lifecycle") }
                                OutlinedButton(onClick = onViewAuditLogs) { Text("Audit logs") }
                                OutlinedButton(onClick = onOpenHelp) { Text("Guide") }
                            }
                            Spacer(Modifier.height(14.dp))
                            if (state.elections.isEmpty()) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text("No election stages yet", fontWeight = FontWeight.Bold, color = SecondaryNavy)
                                        Text("Open a vetting stage to announce roles and invite student applications.", color = TextMuted)
                                    }
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(state.elections) { election ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Column(Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(election.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextDark)
                                                    StatusBadge(status = election.status)
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Text("Academic Year: ${election.academicYear}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                                if (election.status == "Vetting Open" && election.vettingAnnouncement.isNotBlank()) {
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(election.vettingAnnouncement, style = MaterialTheme.typography.bodyMedium, color = TextDark)
                                                }
                                                Spacer(Modifier.height(12.dp))
                                                when (election.status) {
                                                    "Vetting Open" -> Button(
                                                        onClick = { viewModel.closeVettingStage(election.electionId) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                                    ) { Text("Close applications", color = Color.White) }
                                                    "Vetting Closed" -> Button(
                                                        onClick = { onSetupElection(election.electionId) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy),
                                                    ) { Text("Set up election", color = Color.White) }
                                                    "Scheduled" -> {
                                                        Text(
                                                            "Voting: ${election.startDatetime} to ${election.endDatetime}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextMuted,
                                                        )
                                                        OutlinedButton(onClick = { electionToStartEarly = election }) {
                                                            Text("Start voting early")
                                                        }
                                                    }
                                                    "Open" -> Button(
                                                        onClick = { onViewResults(election.electionId) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy),
                                                    ) { Text("View live results", color = Color.White) }
                                                    "Closed", "Results Pending", "Results Published" -> Button(
                                                        onClick = { onViewResults(election.electionId) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy),
                                                    ) { Text("View results", color = Color.White) }
                                                }
                                            }

                                            electionToStartEarly?.let { election ->
                                                AlertDialog(
                                                    onDismissRequest = { electionToStartEarly = null },
                                                    title = { Text("Start election early?") },
                                                    text = {
                                                        Text("Voting for ${election.name} will open now, before its scheduled start time.")
                                                    },
                                                    confirmButton = {
                                                        Button(
                                                            onClick = {
                                                                viewModel.startElectionEarly(election.electionId)
                                                                electionToStartEarly = null
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                                        ) { Text("Start voting now", color = Color.White) }
                                                    },
                                                    dismissButton = {
                                                        TextButton(onClick = { electionToStartEarly = null }) { Text("Cancel") }
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is AdminUiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> Unit
            }
        }
    }
}
