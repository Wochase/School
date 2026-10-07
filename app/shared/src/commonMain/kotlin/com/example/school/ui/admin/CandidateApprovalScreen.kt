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
import com.example.school.data.model.PendingCandidate
import com.example.school.network.ElectionRepository
import kotlinx.coroutines.launch
import com.example.school.ui.*

@Composable
fun CandidateApprovalScreen(
    repository: ElectionRepository,
) {
    val candidatesList = remember { mutableStateListOf<PendingCandidate>() }
    var selectedFilter by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            candidatesList.clear()
            candidatesList.addAll(repository.getPendingCandidates())
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundOffWhite)
                .padding(16.dp)
        ) {
            Text("Candidate applications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
            Spacer(Modifier.height(8.dp))
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending Verification", "Approved", "Rejected").forEach { filter ->
                    FilterChip(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredList = candidatesList.filter {
                if (selectedFilter == "All") true else it.status == selectedFilter
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No candidate applications found.", color = TextMuted, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Student applications will appear here when submitted.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList) { candidate ->
                        CandidateApprovalCard(
                            candidate = candidate,
                            onApprove = {
                                scope.launch {
                                    repository.updateCandidateStatus(candidate.id, "Approved")
                                    candidatesList.clear()
                                    candidatesList.addAll(repository.getPendingCandidates())
                                }
                            },
                            onReject = {
                                scope.launch {
                                    repository.updateCandidateStatus(candidate.id, "Rejected")
                                    candidatesList.clear()
                                    candidatesList.addAll(repository.getPendingCandidates())
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateApprovalCard(
    candidate: PendingCandidate,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(candidate.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("ID: ${candidate.studentId} • ${candidate.classGroup}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                CandidateStatusBadge(status = candidate.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Position: ${candidate.positionName}", fontWeight = FontWeight.Bold, color = SecondaryNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text("\"${candidate.slogan}\"", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = PrimaryDarkOrange)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Manifesto:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(candidate.manifesto, style = MaterialTheme.typography.bodyMedium, color = TextDark)

            Spacer(modifier = Modifier.height(16.dp))

            // Approval Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (candidate.status != "Rejected") {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Reject Candidate")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (candidate.status != "Approved") {
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                    ) {
                        Text("Approve for Ballot", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateStatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Approved" -> StatusGreen to Color.White
        "Rejected" -> Color(0xFFDC2626) to Color.White
        else -> StatusAmber to Color.White
    }

    Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
