package com.example.school.ui.admin

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.school.ui.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onViewResults: (electionId: String) -> Unit,
    onCreateElection: () -> Unit,
    onSetupElection: (electionId: String) -> Unit,
) {
    val adminState by viewModel.adminState.collectAsState()
    var electionToStartEarly by remember { mutableStateOf<com.example.school.data.model.Election?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xffdddbd8))
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Election Stages",
                color = Color.Black,
                style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(16.dp))

            when (val state = adminState) {
                is AdminUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryOrange)
                    }
                }
                is AdminUiState.DashboardLoaded -> {
                    if (state.elections.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xfff8f8f8)),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("No election stages yet", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = SecondaryNavy)
                                Spacer(Modifier.height(8.dp))
                                Text("Open a vetting stage to announce roles and invite student applications.", color = TextMuted)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = onCreateElection, colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)) {
                                    Text("Open Vetting", color = Color.White)
                                }
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(state.elections) { election ->
                                Card(
                                    shape = RoundedCornerShape(15.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(Modifier.padding(20.dp)) {
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
                                        Spacer(Modifier.height(16.dp))
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
                                                Spacer(Modifier.height(8.dp))
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
                is AdminUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                else -> Unit
            }
        }
    }
}
