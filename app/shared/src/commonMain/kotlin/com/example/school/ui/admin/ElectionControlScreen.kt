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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.school.data.model.Election
import com.example.school.data.model.ElectionResults
import com.example.school.network.ElectionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.school.ui.*

@Composable
fun ElectionControlScreen(
    repository: ElectionRepository,
) {
    var elections by remember { mutableStateOf<List<Election>>(emptyList()) }
    var liveResults by remember { mutableStateOf<Map<String, ElectionResults>>(emptyMap()) }
    var electionToStartEarly by remember { mutableStateOf<Election?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultsError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        try {
            val latestElections = repository.getElections()
            elections = latestElections
            errorMessage = null
            val openElections = latestElections.filter { it.status == "Open" }
            val refreshedResults = mutableMapOf<String, ElectionResults>()
            val failedResults = mutableListOf<String>()
            openElections.forEach { election ->
                try {
                    refreshedResults[election.electionId] = repository.getResults(election.electionId)
                } catch (_: Exception) {
                    failedResults.add(election.name)
                }
            }
            liveResults = refreshedResults
            resultsError = failedResults.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "Live vote totals unavailable for: ")
        } catch (_: Exception) {
            errorMessage = "Could not load election lifecycle data."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(repository) {
        while (true) {
            refresh()
            delay(3_000)
        }
    }

    Scaffold { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
            elections.isEmpty() && errorMessage != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Could not load election lifecycle data.", color = MaterialTheme.colorScheme.error)
            }
            elections.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No election records yet. Open a vetting stage to begin.", color = TextMuted)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .background(BackgroundOffWhite).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        Text("Election lifecycle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        Text("Live updates refresh every 3 seconds. Open elections include current vote totals.", color = TextMuted)
                        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        resultsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }
                items(elections) { election ->
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(election.name, fontWeight = FontWeight.Bold, color = SecondaryNavy, style = MaterialTheme.typography.titleMedium)
                                StatusBadge(election.status)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Academic year: ${election.academicYear}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            if (election.startDatetime.isNotBlank() && election.endDatetime.isNotBlank()) {
                                Text(
                                    "Voting window: ${election.startDatetime} to ${election.endDatetime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            when (election.status) {
                                "Vetting Open" -> Text("Student applications are open. Close vetting from Election stages when ready.", color = TextMuted)
                                "Vetting Closed" -> Text("Applications are closed. Set up the ballot from Election stages.", color = TextMuted)
                                "Scheduled" -> StatusActionButton("Start voting early", StatusGreen) {
                                    electionToStartEarly = election
                                }
                                "Open" -> {
                                    liveResults[election.electionId]?.let { results ->
                                        Text(
                                            "Live votes cast: ${results.totalVotesCast}",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusGreen,
                                        )
                                        results.positionResults.forEach { position ->
                                            Text(position.positionName, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
                                            position.results.forEach { candidate ->
                                                Text("${candidate.candidateName}: ${candidate.voteCount} votes", color = TextMuted)
                                            }
                                        }
                                    } ?: Text("Loading live vote totals…", color = TextMuted)
                                    StatusActionButton("Close election", Color.DarkGray) {
                                        scope.launch { updateStatus(repository, election, "Closed", ::refresh, { errorMessage = it }) }
                                    }
                                }
                                "Closed" -> StatusActionButton("Publish results", PrimaryOrange) {
                                    scope.launch { updateStatus(repository, election, "Results Published", ::refresh, { errorMessage = it }) }
                                }
                                "Results Published", "Archived" -> Text("This election lifecycle is complete.", color = TextMuted)
                                else -> Text("This election is currently ${election.status.lowercase()}.", color = TextMuted)
                            }
                            errorMessage?.let {
                                Spacer(Modifier.height(8.dp))
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        electionToStartEarly?.let { election ->
                            AlertDialog(
                                onDismissRequest = { electionToStartEarly = null },
                                title = { Text("Start election early?") },
                                text = { Text("Voting for ${election.name} will open now, before its scheduled start time.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            electionToStartEarly = null
                                            scope.launch { updateStatus(repository, election, "Open", ::refresh, { errorMessage = it }) }
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

private suspend fun updateStatus(
    repository: ElectionRepository,
    election: Election,
    status: String,
    refresh: suspend () -> Unit,
    showError: (String) -> Unit,
) {
    try {
        check(repository.updateElection(election.copy(status = status))) { "The election status could not be updated." }
        refresh()
    } catch (_: Exception) {
        showError("Could not update ${election.name} to $status.")
    }
}

@Composable
private fun StatusActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
