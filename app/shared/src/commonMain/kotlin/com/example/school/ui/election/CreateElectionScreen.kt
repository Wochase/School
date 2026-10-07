package com.example.school.ui.election

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
import com.example.school.data.model.Election
import com.example.school.data.model.PendingCandidate
import com.example.school.network.ElectionRepository
import com.example.school.ui.*
import com.example.school.ui.admin.AdminUiState
import com.example.school.ui.admin.AdminViewModel

@Composable
fun CreateElectionScreen(
    viewModel: AdminViewModel,
    repository: ElectionRepository,
    vettingElectionId: String,
    onElectionCreated: () -> Unit,
    onCancel: () -> Unit,
) {
    var vettingElection by remember { mutableStateOf<Election?>(null) }
    var approvedCandidates by remember { mutableStateOf<List<PendingCandidate>>(emptyList()) }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("08:00") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("17:00") }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val adminState by viewModel.adminState.collectAsState()

    LaunchedEffect(vettingElectionId, repository) {
        try {
            vettingElection = repository.getElections().find { it.electionId == vettingElectionId }
            approvedCandidates = repository.getPendingCandidates().filter {
                it.electionId == vettingElectionId && it.status == "Approved"
            }
            if (vettingElection == null) loadError = "The vetting stage could not be found."
            isLoading = false
        } catch (_: Exception) {
            loadError = "Failed to load approved candidates."
            isLoading = false
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(BackgroundOffWhite).padding(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = PrimaryOrange)
                loadError != null -> Text(loadError!!, color = MaterialTheme.colorScheme.error)
                vettingElection != null -> {
                    Column(
                        modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text("Stage 2: Election setup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                                Spacer(Modifier.height(4.dp))
                                Text(vettingElection!!.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Academic year: ${vettingElection!!.academicYear}", color = TextMuted)
                                Spacer(Modifier.height(8.dp))
                                Text("Only approved applicants are added to the ballot. Choose the voting dates and times in UTC.", color = TextMuted)
                                Spacer(Modifier.height(16.dp))
                                ScheduleDateTimeField(
                                    label = "Voting starts",
                                    date = startDate,
                                    time = startTime,
                                    onDateChange = { startDate = it },
                                    onTimeChange = { startTime = it },
                                )
                                Spacer(Modifier.height(10.dp))
                                ScheduleDateTimeField(
                                    label = "Voting ends",
                                    date = endDate,
                                    time = endTime,
                                    onDateChange = { endDate = it },
                                    onTimeChange = { endTime = it },
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        vettingElection!!.positions.forEach { position ->
                            val candidates = approvedCandidates.filter { it.positionName == position.positionName }
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(position.positionName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                                    if (position.description.isNullOrBlank().not()) {
                                        Text(position.description.orEmpty(), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    if (candidates.isEmpty()) {
                                        Text("No approved applicants for this role; it will not appear on the ballot.", color = TextMuted)
                                    } else {
                                        candidates.forEach { candidate ->
                                            Text("${candidate.fullName} (${candidate.studentId})", fontWeight = FontWeight.SemiBold, color = TextDark)
                                            Text(candidate.manifesto, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            Spacer(Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }

                        val hasApprovedCandidates = approvedCandidates.isNotEmpty()
                        val startDatetime = startDate.toIsoTimestamp(startTime)
                        val endDatetime = endDate.toIsoTimestamp(endTime)
                        val validSchedule = startDatetime.isNotBlank() && endDatetime.isNotBlank() &&
                            startDatetime < endDatetime
                        if (startDatetime.isNotBlank() && endDatetime.isNotBlank() && !validSchedule) {
                            Text("The voting end must be later than the start.", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (!hasApprovedCandidates) {
                            Text("Approve at least one candidate before setting up the election.", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (adminState is AdminUiState.Error) {
                            Text((adminState as AdminUiState.Error).message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                viewModel.createElection(
                                    vettingElectionId = vettingElectionId,
                                    startDatetime = startDatetime.trim(),
                                    endDatetime = endDatetime.trim(),
                                    onSuccess = onElectionCreated,
                                )
                            },
                            enabled = hasApprovedCandidates && validSchedule,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        ) {
                            Text("Create election with approved candidates", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

class PositionFormState(
    initialName: String = "",
    initialDescription: String = "",
) {
    var name by mutableStateOf(initialName)
    var description by mutableStateOf(initialDescription)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ScheduleDateTimeField(
    label: String,
    date: String,
    time: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = SecondaryNavy)
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
            Text(if (date.isBlank()) "Choose date" else date)
        }
        OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
            Text("$time (24-hour)")
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateChange(it.toIsoDate()) }
                        showDatePicker = false
                    },
                ) { Text("Set date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time.take(2).toIntOrNull() ?: 8,
            initialMinute = time.takeLast(2).toIntOrNull() ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            modifier = Modifier.widthIn(min = 520.dp, max = 680.dp),
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choose $label time (UTC)") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TimePicker(
                        state = pickerState,
                        modifier = Modifier.width(520.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange("${pickerState.hour.toString().padStart(2, '0')}:${pickerState.minute.toString().padStart(2, '0')}")
                        showTimePicker = false
                    },
                ) { Text("Set time") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}

private fun Long.toIsoDate(): String {
    val days = this / 86_400_000L
    val z = days + 719_468L
    val era = if (z >= 0) z / 146_097L else (z - 146_096L) / 146_097L
    val dayOfEra = z - era * 146_097L
    val yearOfEra = (dayOfEra - dayOfEra / 1_460L + dayOfEra / 36_524L - dayOfEra / 146_096L) / 365L
    var year = yearOfEra + era * 400L
    val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)
    val monthPart = (5L * dayOfYear + 2L) / 153L
    val day = dayOfYear - (153L * monthPart + 2L) / 5L + 1L
    val month = monthPart + if (monthPart < 10L) 3L else -9L
    if (month <= 2L) year++
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun String.toIsoTimestamp(time: String): String =
    if (length == 10 && time.matches(Regex("\\d{2}:\\d{2}"))) "${this}T${time}:00Z" else ""
