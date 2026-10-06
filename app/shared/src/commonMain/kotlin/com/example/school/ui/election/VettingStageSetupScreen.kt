package com.example.e_voting.ui.election

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
import com.example.e_voting.ui.*
import com.example.e_voting.ui.admin.AdminUiState
import com.example.e_voting.ui.admin.AdminViewModel

@Composable
fun VettingStageSetupScreen(
    viewModel: AdminViewModel,
    onStageCreated: () -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var announcement by remember { mutableStateOf("") }
    val roles = remember { mutableStateListOf<PositionFormState>() }
    val adminState by viewModel.adminState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(BackgroundOffWhite).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Open candidate vetting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        TextButton(onClick = onCancel) { Text("Cancel") }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Stage 1: Candidate vetting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                    Spacer(Modifier.height(6.dp))
                    Text("Publish the election details and roles students can apply for. Applications remain open until you close this stage.", color = TextMuted)
                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(title, { title = it }, label = { Text("Election title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(academicYear, { academicYear = it }, label = { Text("Academic year") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(description, { description = it }, label = { Text("Election details") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        announcement,
                        { announcement = it },
                        label = { Text("Vetting announcement for students") },
                        supportingText = { Text("Explain the application window and anything students should know.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Roles students may apply for", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        Button(onClick = { roles.add(PositionFormState()) }) {
                            Text("Add role")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    roles.forEachIndexed { index, role ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = BackgroundOffWhite)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = role.name,
                                        onValueChange = { role.name = it },
                                        label = { Text("Role ${index + 1}") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(onClick = { roles.removeAt(index) }) { Text("Remove") }
                                }
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = role.description,
                                    onValueChange = { role.description = it },
                                    label = { Text("What students should know about this role") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                )
                            }
                        }
                    }
                    if (roles.isEmpty()) {
                        Text("Add at least one role before opening applications.", color = TextMuted)
                    }
                    if (adminState is AdminUiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text((adminState as AdminUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = onCancel) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.createVettingStage(
                                    title = title.trim(),
                                    academicYear = academicYear.trim(),
                                    description = description.trim(),
                                    announcement = announcement.trim(),
                                    roles = roles.map { it.name.trim() to it.description.trim() },
                                    onSuccess = onStageCreated,
                                )
                            },
                            enabled = title.isNotBlank() && academicYear.isNotBlank() &&
                                announcement.isNotBlank() && roles.isNotEmpty() &&
                                roles.all { it.name.isNotBlank() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        ) {
                            Text("Publish vetting stage", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
