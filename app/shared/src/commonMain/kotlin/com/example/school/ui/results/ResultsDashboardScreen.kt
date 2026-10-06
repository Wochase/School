package com.example.e_voting.ui.results

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
import com.example.e_voting.data.model.PositionResult
import kotlinx.coroutines.delay
import com.example.e_voting.ui.*
import com.example.e_voting.ui.admin.AdminUiState
import com.example.e_voting.ui.admin.AdminViewModel

@Composable
fun ResultsDashboardScreen(
    viewModel: AdminViewModel,
    electionId: String,
) {
    val adminState by viewModel.adminState.collectAsState()

    LaunchedEffect(electionId) {
        while (true) {
            viewModel.fetchElectionResults(electionId)
            delay(5_000)
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundOffWhite)) {
            when (val state = adminState) {
                is AdminUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryOrange)
                is AdminUiState.DashboardLoaded -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Live election results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        Text("Automatically refreshes every 5 seconds while voting is open.", color = TextMuted)
                        Spacer(Modifier.height(14.dp))
                        state.activeResults?.let { results ->
                            // Turnout Stats KPI Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    "Eligible Voters",
                                    results.totalEligibleVoters.takeIf { it > 0 }?.toString() ?: "Not available",
                                    modifier = Modifier.weight(1f),
                                )
                                StatCard("Votes Cast", results.totalVotesCast.toString(), modifier = Modifier.weight(1f))
                                StatCard(
                                    "Turnout",
                                    results.totalEligibleVoters.takeIf { it > 0 }?.let { "${results.turnoutPercentage}%" } ?: "Not available",
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(results.positionResults) { posResult ->
                                    PositionResultCard(posResult)
                                }
                            }
                        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading current vote totals…", color = TextMuted)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryOrange)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

@Composable
fun PositionResultCard(posResult: PositionResult) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(posResult.positionName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
            Spacer(modifier = Modifier.height(12.dp))

            posResult.results.forEach { cand ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cand.candidateName, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("${cand.voteCount} votes (${cand.percentage}%)", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (cand.percentage / 100.0).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = PrimaryOrange,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
