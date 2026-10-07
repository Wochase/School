package com.example.school.ui.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.school.ui.admin.AdminUiState
import com.example.school.ui.admin.AdminViewModel
import school.app.shared.generated.resources.Res
import school.app.shared.generated.resources.schoollogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun StudentDashboardScreen(
    viewModel: AdminViewModel,
    studentId: String,
    onSelectElectionToVote: (String) -> Unit,
    onViewPublishedResults: (String) -> Unit,
    onNavigateElections: () -> Unit = {},
    onNavigateApply: () -> Unit = {},
    onNavigateInbox: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val adminState by viewModel.adminState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xffdddbd8))
    ) {
        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 260.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Elections",
                    color = Color.Black,
                    style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(24.dp))

                when (val state = adminState) {
                    is AdminUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xfff08d5e))
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
                                    Text("There are no ongoing elections", color = Color.Black, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Come back later", color = Color.DarkGray, style = TextStyle(fontSize = 18.sp))
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(state.elections) { election ->
                                    ElectionCardItem(
                                        election = election,
                                        onVoteClick = { onSelectElectionToVote(election.electionId) },
                                        onResultsClick = { onViewPublishedResults(election.electionId) }
                                    )
                                }
                            }
                        }
                    }
                    is AdminUiState.Error -> {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> Unit
                }
            }
        }

        // Sidebar Navigation Panel (Compact Width 260.dp)
        StudentSidebar(
            activeTab = "Elections",
            onNavigateElections = onNavigateElections,
            onNavigateApply = onNavigateApply,
            onNavigateInbox = onNavigateInbox,
            onLogout = onLogout
        )
    }
}

@Composable
fun ElectionCardItem(
    election: Election,
    onVoteClick: () -> Unit,
    onResultsClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(election.name, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                Surface(
                    color = when (election.status) {
                        "Open" -> Color(0xFF10B981)
                        "Results Published" -> Color(0xFF3B82F6)
                        else -> Color(0xFFF59E0B)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(election.status, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(election.description, style = TextStyle(fontSize = 14.sp), color = Color.DarkGray)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                when (election.status) {
                    "Open" -> Button(
                        onClick = onVoteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff08d5e))
                    ) {
                        Text("Proceed to Ballot", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    "Results Published" -> OutlinedButton(
                        onClick = onResultsClick
                    ) {
                        Text("View Results", color = Color(0xfff08d5e), fontWeight = FontWeight.Bold)
                    }
                    else -> Text("Voting opens soon", style = TextStyle(fontSize = 14.sp), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StudentSidebar(
    activeTab: String,
    onNavigateElections: () -> Unit,
    onNavigateApply: () -> Unit,
    onNavigateInbox: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(color = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // School Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.schoollogo),
                    contentDescription = "SchoolLogo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Student Portal",
                    color = Color.Black,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xff9a9a9a), thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

                // Elections Button
                SidebarButton(
                    text = "Elections",
                    selected = activeTab == "Elections",
                    onClick = onNavigateElections
                )
                Spacer(Modifier.height(10.dp))

                // Apply for a role Button
                SidebarButton(
                    text = "Apply for a role",
                    selected = activeTab == "Apply",
                    onClick = onNavigateApply
                )
                Spacer(Modifier.height(10.dp))

                // Inbox Button
                SidebarButton(
                    text = "Inbox",
                    selected = activeTab == "Inbox",
                    onClick = onNavigateInbox
                )
            }
        }

        // Logout Button at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(180.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Logout",
                color = Color.White,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SidebarButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(if (selected) 16.dp else 8.dp))
            .background(color = if (selected) Color(0xfff0efa7) else Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xff1f1f02) else Color.Black,
            style = TextStyle(fontSize = 18.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
