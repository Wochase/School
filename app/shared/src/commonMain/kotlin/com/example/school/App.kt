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
import com.example.school.ui.admin.AdminMainRoot
import com.example.school.ui.admin.AdminViewModel
import com.example.school.ui.auth.LoginScreen
import com.example.school.ui.auth.AuthViewModel
import com.example.school.ui.inbox.InboxScreen
import com.example.school.ui.inbox.InboxViewModel
import com.example.school.ui.results.ResultsDashboardScreen
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
    object Inbox : Screen
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
                            AdminMainRoot(
                                viewModel = adminViewModel,
                                inboxViewModel = inboxViewModel,
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = Screen.Login
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
