package com.example.school


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.school.data.AppContainer
import com.example.school.ui.*
import com.example.school.ui.admin.AdminDashboardScreen
import com.example.school.ui.admin.AdminHelpScreen
import com.example.school.ui.admin.AdminViewModel
import com.example.school.ui.admin.AuditLogsScreen
import com.example.school.ui.admin.CandidateApprovalScreen
import com.example.school.ui.admin.ElectionControlScreen
import com.example.school.ui.auth.LoginScreen
import com.example.school.ui.auth.AuthViewModel
import com.example.school.ui.election.CreateElectionScreen
import com.example.school.ui.election.VettingStageSetupScreen
import com.example.school.ui.inbox.InboxScreen
import com.example.school.ui.inbox.InboxViewModel
import com.example.school.ui.results.ResultsDashboardScreen
import com.example.school.ui.student.CandidateApplicationScreen
import com.example.school.ui.student.StudentMainRoot
import com.example.school.ui.voting.BallotScreen
import com.example.school.ui.voting.VotingViewModel
import com.example.school.ui.voting.VoteConfirmationScreen
import kotlinx.coroutines.delay

sealed interface Screen {
    object Login : Screen
    data class StudentDashboard(val studentId: String) : Screen
    data class StudentBallot(val studentId: String, val electionId: String) : Screen
    data class VoteReceipt(val confirmationCode: String) : Screen
    object AdminDashboard : Screen
    object CandidateApproval : Screen
    object ElectionControl : Screen
    object AuditLogs : Screen
    object VettingStageSetup : Screen
    data class CreateElection(val vettingElectionId: String) : Screen
    object Inbox : Screen
    object AdminHelp : Screen
    data class CandidateApplication(val studentId: String) : Screen
    data class PublishedResults(val electionId: String) : Screen
}

@Composable
fun App() {
    val repository = AppContainer.repository
    val authViewModel = remember(repository) { AuthViewModel(repository) }
    val votingViewModel = remember(repository) { VotingViewModel(repository) }
    val adminViewModel = remember(repository) { AdminViewModel(repository) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var loggedInStudentId by remember { mutableStateOf("s100") }
    var userRole by remember { mutableStateOf("Student") }
    val isAdmin = userRole != "Student"
    val inboxViewModel = remember(repository, isAdmin, loggedInStudentId) {
        InboxViewModel(repository, isAdmin, loggedInStudentId)
    }
    val inboxState by inboxViewModel.state.collectAsState()

    LaunchedEffect(inboxViewModel, currentScreen !is Screen.Login) {
        if (currentScreen !is Screen.Login) {
            while (true) {
                inboxViewModel.refresh()
                delay(2_000)
            }
        }
    }

    EVotingTheme {
        Surface {
            Row(Modifier.fillMaxSize()) {
                if (currentScreen !is Screen.Login && isAdmin) {
                    PersistentNavigationPanel(
                        isAdmin = isAdmin,
                        studentId = loggedInStudentId,
                        unreadCount = inboxState.unreadCount,
                        currentScreen = currentScreen,
                        onNavigate = { currentScreen = it },
                        onLogout = {
                            authViewModel.logout()
                            currentScreen = Screen.Login
                        },
                    )
                    VerticalDivider()
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { role, studentId ->
                                    userRole = role
                                    if (role == "Student") {
                                        loggedInStudentId = studentId ?: "s100"
                                        currentScreen = Screen.StudentDashboard(loggedInStudentId)
                                    } else {
                                        currentScreen = Screen.AdminDashboard
                                    }
                                }
                            )
                        }
                        is Screen.StudentDashboard -> {
                            StudentMainRoot(
                                viewModel = adminViewModel,
                                inboxViewModel = inboxViewModel,
                                studentId = screen.studentId,
                                onSelectElectionToVote = { electionId ->
                                    currentScreen = Screen.StudentBallot(screen.studentId, electionId)
                                },
                                onViewPublishedResults = { electionId ->
                                    currentScreen = Screen.PublishedResults(electionId)
                                },
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                        is Screen.CandidateApplication -> {
                            CandidateApplicationScreen(
                                repository = repository,
                                studentId = screen.studentId,
                                onApplicationSubmitted = {
                                    currentScreen = Screen.StudentDashboard(screen.studentId)
                                },
                                onCancel = {
                                    currentScreen = Screen.StudentDashboard(screen.studentId)
                                }
                            )
                        }
                        is Screen.StudentBallot -> {
                            BallotScreen(
                                viewModel = votingViewModel,
                                studentId = screen.studentId,
                                electionId = screen.electionId,
                                onVoteComplete = { code ->
                                    currentScreen = Screen.VoteReceipt(code)
                                }
                            )
                        }
                        is Screen.VoteReceipt -> {
                            VoteConfirmationScreen(
                                confirmationCode = screen.confirmationCode,
                                onReturnToDashboard = {
                                    currentScreen = Screen.StudentDashboard(loggedInStudentId)
                                }
                            )
                        }
                        is Screen.AdminDashboard -> {
                            AdminDashboardScreen(
                                viewModel = adminViewModel,
                                onViewResults = { electionId ->
                                    currentScreen = Screen.PublishedResults(electionId)
                                },
                                onCreateElection = {
                                    currentScreen = Screen.VettingStageSetup
                                },
                                onSetupElection = { electionId ->
                                    currentScreen = Screen.CreateElection(electionId)
                                },
                                onManageCandidates = {
                                    currentScreen = Screen.CandidateApproval
                                },
                                onElectionControl = {
                                    currentScreen = Screen.ElectionControl
                                },
                                onViewAuditLogs = {
                                    currentScreen = Screen.AuditLogs
                                },
                                onOpenHelp = {
                                    currentScreen = Screen.AdminHelp
                                },
                            )
                        }
                        is Screen.AdminHelp -> {
                            AdminHelpScreen()
                        }
                        is Screen.CandidateApproval -> {
                            CandidateApprovalScreen(
                                repository = repository,
                            )
                        }
                        is Screen.ElectionControl -> {
                            ElectionControlScreen(
                                repository = repository,
                            )
                        }
                        is Screen.AuditLogs -> {
                            AuditLogsScreen(
                                repository = repository,
                            )
                        }
                        is Screen.VettingStageSetup -> {
                            VettingStageSetupScreen(
                                viewModel = adminViewModel,
                                onStageCreated = { currentScreen = Screen.AdminDashboard },
                                onCancel = { currentScreen = Screen.AdminDashboard }
                            )
                        }
                        is Screen.CreateElection -> {
                            CreateElectionScreen(
                                viewModel = adminViewModel,
                                repository = repository,
                                vettingElectionId = screen.vettingElectionId,
                                onElectionCreated = {
                                    currentScreen = Screen.AdminDashboard
                                },
                                onCancel = {
                                    currentScreen = Screen.AdminDashboard
                                }
                            )
                        }
                        is Screen.PublishedResults -> {
                            ResultsDashboardScreen(
                                viewModel = adminViewModel,
                                electionId = screen.electionId,
                            )
                        }
                        is Screen.Inbox -> {
                            InboxScreen(
                                viewModel = inboxViewModel,
                                isAdmin = isAdmin,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersistentNavigationPanel(
    isAdmin: Boolean,
    studentId: String,
    unreadCount: Int,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier.width(210.dp).fillMaxHeight().background(Color.White).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (isAdmin) "ADMIN PORTAL" else "STUDENT PORTAL",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = SecondaryNavy,
        )
        HorizontalDivider()
        if (isAdmin) {
            NavigationItem("Election stages", currentScreen is Screen.AdminDashboard) { onNavigate(Screen.AdminDashboard) }
            NavigationItem("Open vetting", currentScreen is Screen.VettingStageSetup) { onNavigate(Screen.VettingStageSetup) }
            NavigationItem("Candidate review", currentScreen is Screen.CandidateApproval) { onNavigate(Screen.CandidateApproval) }
            NavigationItem("Election lifecycle", currentScreen is Screen.ElectionControl) { onNavigate(Screen.ElectionControl) }
            NavigationItem("Audit logs", currentScreen is Screen.AuditLogs) { onNavigate(Screen.AuditLogs) }
            NavigationItem("Admin guide", currentScreen is Screen.AdminHelp) { onNavigate(Screen.AdminHelp) }
        } else {
            NavigationItem("Elections", currentScreen is Screen.StudentDashboard) {
                onNavigate(Screen.StudentDashboard(studentId))
            }
            NavigationItem("Apply for a role", currentScreen is Screen.CandidateApplication) {
                onNavigate(Screen.CandidateApplication(studentId))
            }
        }
        HorizontalDivider()
        NavigationItem("Inbox", currentScreen is Screen.Inbox, unreadCount) { onNavigate(Screen.Inbox) }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Log out") }
    }
}

@Composable
private fun NavigationItem(label: String, selected: Boolean, onClick: () -> Unit) =
    NavigationItem(label, selected, 0, onClick)

@Composable
private fun NavigationItem(label: String, selected: Boolean, unreadCount: Int, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) PrimaryOrange.copy(alpha = 0.12f) else Color.Transparent,
            contentColor = if (selected) PrimaryOrange else SecondaryNavy,
        ),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            if (unreadCount > 0) {
                Surface(color = PrimaryOrange, shape = MaterialTheme.shapes.small) {
                    Text(
                        unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
