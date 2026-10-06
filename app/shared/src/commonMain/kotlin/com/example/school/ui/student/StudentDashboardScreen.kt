package com.example.e_voting.ui.student

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
import com.example.e_voting.data.model.Election
import com.example.e_voting.ui.*
import com.example.e_voting.ui.admin.AdminUiState
import com.example.e_voting.ui.admin.AdminViewModel

@Composable
fun StudentDashboardScreen(
    viewModel: AdminViewModel,
    studentId: String,
    onSelectElectionToVote: (electionId: String) -> Unit,
    onViewPublishedResults: (electionId: String) -> Unit,
) {
    val adminState by viewModel.adminState.collectAsState()

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
                            Text("Student elections", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            Text("Welcome, Student ID: $studentId", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(Modifier.height(16.dp))
                            if (state.elections.isEmpty()) {
                                Text("There are no elections available yet.", color = TextMuted)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(state.elections) { election ->
                                        StudentElectionCard(
                                            election = election,
                                            onVoteClick = { onSelectElectionToVote(election.electionId) },
                                            onResultsClick = { onViewPublishedResults(election.electionId) },
                                        )
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

@Composable
fun StudentElectionCard(
    election: Election,
    onVoteClick: () -> Unit,
    onResultsClick: () -> Unit,
) {
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
                Text(election.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                StatusBadge(status = election.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(election.description, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Spacer(Modifier.height(12.dp))
            when (election.status) {
                "Open" -> Button(
                    onClick = onVoteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End),
                ) { Text("Proceed to Ballot", color = Color.White, fontWeight = FontWeight.Bold) }
                "Results Published" -> OutlinedButton(
                    onClick = onResultsClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End),
                ) { Text("View Official Results", color = PrimaryOrange, fontWeight = FontWeight.Bold) }
                "Vetting Open" -> {
                    Text("Candidate applications are open. See the side panel for details.", color = TextMuted)
                    election.positions.forEach { position ->
                        Text("${position.positionName}: ${position.description.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                else -> Text("Voting opens soon or is currently under review.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}
