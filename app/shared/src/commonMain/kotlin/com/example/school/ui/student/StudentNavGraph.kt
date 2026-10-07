package com.example.school.ui.student

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.school.data.AppContainer
import com.example.school.network.ElectionRepository
import com.example.school.ui.admin.AdminViewModel
import com.example.school.ui.inbox.InboxViewModel

sealed class StudentScreen(val route: String) {
    object Dashboard : StudentScreen("student_dashboard")
    object Apply : StudentScreen("candidate_apply")
    object Inbox : StudentScreen("student_inbox")
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
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = StudentScreen.Dashboard.route
    ) {
        composable(StudentScreen.Dashboard.route) {
            StudentDashboardScreen(
                viewModel = viewModel,
                studentId = studentId,
                onSelectElectionToVote = onSelectElectionToVote,
                onViewPublishedResults = onViewPublishedResults,
                onNavigateElections = { navController.navigate(StudentScreen.Dashboard.route) { launchSingleTop = true } },
                onNavigateApply = { navController.navigate(StudentScreen.Apply.route) { launchSingleTop = true } },
                onNavigateInbox = { navController.navigate(StudentScreen.Inbox.route) { launchSingleTop = true } },
                onLogout = onLogout
            )
        }
        composable(StudentScreen.Apply.route) {
            CandidateApplicationScreen(
                repository = repository,
                studentId = studentId,
                onApplicationSubmitted = {
                    navController.navigate(StudentScreen.Dashboard.route) {
                        popUpTo(StudentScreen.Dashboard.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                },
                onNavigateElections = { navController.navigate(StudentScreen.Dashboard.route) { launchSingleTop = true } },
                onNavigateApply = { navController.navigate(StudentScreen.Apply.route) { launchSingleTop = true } },
                onNavigateInbox = { navController.navigate(StudentScreen.Inbox.route) { launchSingleTop = true } },
                onLogout = onLogout
            )
        }
        composable(StudentScreen.Inbox.route) {
            StudentInboxScreen(
                studentId = studentId,
                inboxViewModel = inboxViewModel,
                onNavigateElections = { navController.navigate(StudentScreen.Dashboard.route) { launchSingleTop = true } },
                onNavigateApply = { navController.navigate(StudentScreen.Apply.route) { launchSingleTop = true } },
                onNavigateInbox = { navController.navigate(StudentScreen.Inbox.route) { launchSingleTop = true } },
                onLogout = onLogout
            )
        }
    }
}
