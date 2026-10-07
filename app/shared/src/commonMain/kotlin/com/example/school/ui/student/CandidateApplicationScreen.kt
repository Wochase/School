package com.example.school.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.school.data.model.Election
import com.example.school.data.model.PendingCandidate
import com.example.school.network.ElectionRepository
import kotlinx.coroutines.launch

@Composable
fun CandidateApplicationScreen(
    repository: ElectionRepository,
    studentId: String,
    onApplicationSubmitted: () -> Unit,
    onCancel: () -> Unit,
    onNavigateElections: () -> Unit = {},
    onNavigateApply: () -> Unit = {},
    onNavigateInbox: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var elections by remember { mutableStateOf<List<Election>>(emptyList()) }
    var selectedElection by remember { mutableStateOf<Election?>(null) }
    var selectedPosition by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var classGroup by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }
    var manifesto by remember { mutableStateOf("") }
    var submittedSuccess by remember { mutableStateOf(false) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xffdddbd8))
    ) {
        // Main Content Area matching ApplyScreen Figma UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 260.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp)
            ) {
                Text(
                    text = "Apply",
                    color = Color.Black,
                    style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(color = Color(0xfff8f8f8))
                        .padding(24.dp)
                ) {
                    if (submittedSuccess) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Application Submitted Successfully!", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold), color = Color(0xfff08d5e))
                            Spacer(Modifier.height(12.dp))
                            Text("Your application is pending admin review.", color = Color.DarkGray)
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = onApplicationSubmitted, colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff08d5e))) {
                                Text("Return to Elections", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (elections.isEmpty()) {
                        Column {
                            Text("There are no available roles", color = Color.Black, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(8.dp))
                            Text("Come back later", color = Color.DarkGray, style = TextStyle(fontSize = 18.sp))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Submit Vetting Application", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color.Black)

                            if (submissionError != null) {
                                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                                    Text(submissionError!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                                }
                            }

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = classGroup,
                                onValueChange = { classGroup = it },
                                label = { Text("Class / Group") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = slogan,
                                onValueChange = { slogan = it },
                                label = { Text("Campaign Slogan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = manifesto,
                                onValueChange = { manifesto = it },
                                label = { Text("Manifesto / Suitability Statement") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = onCancel) { Text("Cancel") }
                                Spacer(Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        if (fullName.isBlank() || selectedElection == null) {
                                            submissionError = "Please fill in all required fields."
                                            return@Button
                                        }
                                        scope.launch {
                                            try {
                                                repository.submitCandidateApplication(
                                                    PendingCandidate(
                                                        id = "app_${studentId}_${selectedElection!!.electionId}",
                                                        electionId = selectedElection!!.electionId,
                                                        fullName = fullName,
                                                        studentId = studentId,
                                                        classGroup = classGroup.ifBlank { "General" },
                                                        positionName = selectedPosition.ifBlank { "Representative" },
                                                        slogan = slogan,
                                                        manifesto = manifesto,
                                                        status = "Pending Verification"
                                                    )
                                                )
                                                submittedSuccess = true
                                            } catch (e: Exception) {
                                                submissionError = e.message ?: "Failed to submit application."
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff08d5e))
                                ) {
                                    Text("Submit Application", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sidebar Navigation Panel
        StudentSidebar(
            activeTab = "Apply",
            onNavigateElections = onNavigateElections,
            onNavigateApply = onNavigateApply,
            onNavigateInbox = onNavigateInbox,
            onLogout = onLogout
        )
    }
}
