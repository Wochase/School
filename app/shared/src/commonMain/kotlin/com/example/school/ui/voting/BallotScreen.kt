package com.example.school.ui.voting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.school.data.model.Candidate
import com.example.school.data.model.Position
import com.example.school.ui.*
import com.example.school.ui.voting.VotingUiState
import com.example.school.ui.voting.VotingViewModel

@Composable
fun BallotScreen(
    viewModel: VotingViewModel,
    studentId: String,
    electionId: String,
    onVoteComplete: (confirmationCode: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(electionId, studentId) {
        viewModel.loadBallotForStudent(electionId, studentId)
    }

    LaunchedEffect(uiState) {
        if (uiState is VotingUiState.Submitted) {
            onVoteComplete((uiState as VotingUiState.Submitted).confirmationCode)
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BackgroundOffWhite)) {
            when (val state = uiState) {
                is VotingUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryOrange)
                }
                is VotingUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Notice", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = TextMuted)
                    }
                }
                is VotingUiState.BallotLoaded -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Official ballot",
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy,
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.election.positions) { position ->
                                PositionSection(
                                    position = position,
                                    selectedCandidateId = state.selections[position.positionId],
                                    onSelectCandidate = { candidateId ->
                                        viewModel.selectCandidate(position.positionId, candidateId)
                                    }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // Bottom Action Footer Bar
                        Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.selections.size} of ${state.election.positions.size} Selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                                Button(
                                    onClick = { viewModel.setReviewModalVisible(true) },
                                    enabled = state.selections.size == state.election.positions.size,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Review Vote", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Vote Review Popup Modal
                    if (state.showReviewModal) {
                        VoteReviewModal(
                            positions = state.election.positions,
                            selections = state.selections,
                            onDismiss = { viewModel.setReviewModalVisible(false) },
                            onConfirm = { viewModel.confirmAndSubmitVote(studentId) }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun PositionSection(
    position: Position,
    selectedCandidateId: String?,
    onSelectCandidate: (candidateId: String) -> Unit
) {
    Column {
        Text(position.positionName, style = MaterialTheme.typography.titleLarge, color = SecondaryNavy, fontWeight = FontWeight.Bold)
        Text(position.description ?: "Select ${position.maxSelections} candidate", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        position.candidates.forEach { candidate ->
            CandidateCard(
                candidate = candidate,
                isSelected = candidate.candidateId == selectedCandidateId,
                onSelect = { onSelectCandidate(candidate.candidateId) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CandidateCard(
    candidate: Candidate,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryOrange else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryOrange)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(candidate.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                candidate.slogan?.let {
                    Text("\"$it\"", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = PrimaryDarkOrange)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(candidate.manifesto, style = MaterialTheme.typography.bodyMedium, color = TextMuted, maxLines = 2)
            }
        }
    }
}

@Composable
fun VoteReviewModal(
    positions: List<Position>,
    selections: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Your Vote", fontWeight = FontWeight.Bold, color = SecondaryNavy) },
        text = {
            Column {
                // Mandatory Security Warning Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusAmber.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "You are about to submit your vote. Once submitted, your vote cannot be changed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryNavy,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                positions.forEach { pos ->
                    val candId = selections[pos.positionId]
                    val candName = pos.candidates.find { it.candidateId == candId }?.fullName ?: "None"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pos.positionName, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        Text(candName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text("Submit Vote", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Return to Ballot", color = TextMuted)
            }
        }
    )
}
