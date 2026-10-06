package com.example.e_voting.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_voting.data.model.Election
import com.example.e_voting.data.model.PendingCandidate
import com.example.e_voting.network.ElectionRepository
import kotlinx.coroutines.launch
import com.example.e_voting.ui.*

@Composable
fun CandidateApplicationScreen(
    repository: ElectionRepository,
    studentId: String,
    onApplicationSubmitted: () -> Unit,
    onCancel: () -> Unit,
) {
    var elections by remember { mutableStateOf<List<Election>>(emptyList()) }
    var selectedElection by remember { mutableStateOf<Election?>(null) }
    var selectedPosition by remember { mutableStateOf<String>("") }

    var fullName by remember { mutableStateOf("") }
    var classGroup by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }
    var manifesto by remember { mutableStateOf("") } // Suitability statement
    var submittedSuccess by remember { mutableStateOf(value = false) }
    var submissionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val list = repository.getElections().filter { it.status == "Vetting Open" }
            elections = list
            selectedElection = list.firstOrNull()
            selectedPosition = list.firstOrNull()?.positions?.firstOrNull()?.positionName.orEmpty()
        } catch (_: Exception) {
            submissionError = "Could not load open candidate applications."
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundOffWhite)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp)
                ) {
                    if (submittedSuccess) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(StatusGreen, shape = RoundedCornerShape(32.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Application Submitted!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your nomination has been sent to the electoral committee for review and verification.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onApplicationSubmitted,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy)
                            ) {
                                Text("Return to Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Apply to run as a candidate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            TextButton(onClick = onCancel) { Text("Cancel") }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Select an open vetting stage and role, then explain why you suit it.", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = studentId,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Student ID") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.LightGray)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryOrange)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = classGroup,
                            onValueChange = { classGroup = it },
                            label = { Text("Class / Grade (e.g., Grade 12 - Science)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryOrange)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Election Selector
                        Text("Select Election:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (elections.isEmpty()) {
                            Text("There are no open vetting stages at this time.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        } else {
                            elections.forEach { election ->
                                val isSelected = selectedElection?.electionId == election.electionId
                                Surface(
                                    onClick = {
                                        selectedElection = election
                                        selectedPosition = election.positions.firstOrNull()?.positionName.orEmpty()
                                    },
                                    color = if (isSelected) PrimaryOrange.copy(alpha = 0.15f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(election.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = TextDark)
                                        StatusBadge(status = election.status)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Position / Role Selector from Admin defined positions
                        Text("Select Position / Role:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        selectedElection?.positions?.forEach { pos ->
                            val isSelectedPos = selectedPosition == pos.positionName
                            Surface(
                                onClick = { selectedPosition = pos.positionName },
                                color = if (isSelectedPos) SecondaryNavy.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelectedPos, onClick = { selectedPosition = pos.positionName })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(pos.positionName, fontWeight = if (isSelectedPos) FontWeight.Bold else FontWeight.Normal, color = TextDark)
                                        if (pos.description.isNullOrBlank().not()) {
                                            Text(pos.description.orEmpty(), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }

                        selectedElection?.vettingAnnouncement?.takeIf { it.isNotBlank() }?.let { announcement ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("From the election administrator:", fontWeight = FontWeight.Bold, color = SecondaryNavy)
                            Text(announcement, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = slogan,
                            onValueChange = { slogan = it },
                            label = { Text("Campaign Slogan / Motto") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryOrange)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = manifesto,
                            onValueChange = { manifesto = it },
                            label = { Text("Suitability Statement (Why you suit this role)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryOrange)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (fullName.isNotBlank() && classGroup.isNotBlank() && manifesto.isNotBlank() &&
                                    selectedElection?.status == "Vetting Open" && selectedPosition.isNotBlank()
                                ) {
                                    scope.launch {
                                        try {
                                            val newApp = PendingCandidate(
                                                id = "cand_${(1000..9999).random()}",
                                                electionId = selectedElection!!.electionId,
                                                fullName = fullName,
                                                studentId = studentId,
                                                classGroup = classGroup,
                                                positionName = selectedPosition,
                                                slogan = slogan,
                                                manifesto = manifesto,
                                                status = "Pending Verification",
                                            )
                                            repository.submitCandidateApplication(newApp)
                                            submittedSuccess = true
                                        } catch (_: Exception) {
                                            submissionError = "The application could not be submitted. Applications may have closed."
                                        }
                                    }
                                }
                            },
                            enabled = fullName.isNotBlank() && classGroup.isNotBlank() && manifesto.isNotBlank() &&
                                selectedElection?.status == "Vetting Open" && selectedPosition.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            Text("Submit Nomination Form", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        submissionError?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
