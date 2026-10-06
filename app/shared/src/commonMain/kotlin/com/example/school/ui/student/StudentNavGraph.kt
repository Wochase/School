package com.example.e_voting.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.e_voting.data.AppContainer
import com.example.e_voting.network.ElectionRepository
import com.example.e_voting.ui.PrimaryOrange
import com.example.e_voting.ui.SecondaryNavy
import com.example.e_voting.ui.admin.AdminViewModel
import com.example.e_voting.ui.inbox.InboxScreen
import com.example.e_voting.ui.inbox.InboxViewModel

sealed class StudentScreen(val title: String) {
    object Dashboard : StudentScreen("Dashboard")
    object Apply : StudentScreen("Apply for Role")
    object Inbox : StudentScreen("Inbox")
}

@Composable
fun StudentMainRoot(
    viewModel: AdminViewModel,
    repository: ElectionRepository = AppContainer.repository,
    inboxViewModel: InboxViewModel,
    studentId: String,
    onSelectElectionToVote: (String) -> Unit,
    onViewPublishedResults: (String) -> Unit,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<StudentScreen>(StudentScreen.Dashboard) }

    Scaffold(
        bottomBar = {
            StudentBottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentScreen) {
                StudentScreen.Dashboard -> {
                    StudentDashboardScreen(
                        viewModel = viewModel,
                        studentId = studentId,
                        onSelectElectionToVote = onSelectElectionToVote,
                        onViewPublishedResults = onViewPublishedResults
                    )
                }
                StudentScreen.Apply -> {
                    CandidateApplicationScreen(
                        repository = repository,
                        studentId = studentId,
                        onApplicationSubmitted = { currentScreen = StudentScreen.Dashboard },
                        onCancel = { currentScreen = StudentScreen.Dashboard }
                    )
                }
                StudentScreen.Inbox -> {
                    InboxScreen(
                        viewModel = inboxViewModel,
                        isAdmin = false
                    )
                }
            }
        }
    }
}

@Composable
fun StudentBottomBar(
    currentScreen: StudentScreen,
    onScreenSelected: (StudentScreen) -> Unit
) {
    val items = listOf(
        StudentScreen.Dashboard,
        StudentScreen.Apply,
        StudentScreen.Inbox
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = SecondaryNavy
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { /* Add icons based on screen if needed */ },
                label = { Text(screen.title) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryOrange,
                    selectedTextColor = PrimaryOrange,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
