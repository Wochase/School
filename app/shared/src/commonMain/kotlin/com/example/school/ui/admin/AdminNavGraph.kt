package com.example.school.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.school.data.AppContainer
import com.example.school.network.ElectionRepository
import com.example.school.ui.election.CreateElectionScreen
import com.example.school.ui.election.VettingStageSetupScreen
import com.example.school.ui.inbox.InboxScreen
import com.example.school.ui.inbox.InboxViewModel
import com.example.school.ui.results.ResultsDashboardScreen

sealed interface AdminScreen {
    object Dashboard : AdminScreen
    object VettingSetup : AdminScreen
    object CandidateApproval : AdminScreen
    object ElectionControl : AdminScreen
    object Help : AdminScreen
    object Inbox : AdminScreen
    data class CreateElection(val vettingElectionId: String) : AdminScreen
    data class Results(val electionId: String) : AdminScreen
}

@Composable
fun AdminMainRoot(
    viewModel: AdminViewModel,
    repository: ElectionRepository = AppContainer.repository,
    inboxViewModel: InboxViewModel,
    onLogout: () -> Unit
) {
    var currentAdminScreen by remember { mutableStateOf<AdminScreen>(AdminScreen.Dashboard) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xffdddbd8))) {
        Box(modifier = Modifier.fillMaxSize().padding(start = 260.dp)) {
            when (val screen = currentAdminScreen) {
                is AdminScreen.Dashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onViewResults = { electionId -> currentAdminScreen = AdminScreen.Results(electionId) },
                        onCreateElection = { currentAdminScreen = AdminScreen.VettingSetup },
                        onSetupElection = { electionId -> currentAdminScreen = AdminScreen.CreateElection(electionId) },
                    )
                }
                is AdminScreen.VettingSetup -> {
                    VettingStageSetupScreen(
                        viewModel = viewModel,
                        onStageCreated = { currentAdminScreen = AdminScreen.Dashboard },
                        onCancel = { currentAdminScreen = AdminScreen.Dashboard }
                    )
                }
                is AdminScreen.CandidateApproval -> {
                    CandidateApprovalScreen(
                        repository = repository,
                    )
                }
                is AdminScreen.ElectionControl -> {
                    ElectionControlScreen(
                        repository = repository,
                    )
                }
                is AdminScreen.Help -> {
                    AdminHelpScreen()
                }
                is AdminScreen.Inbox -> {
                    InboxScreen(
                        viewModel = inboxViewModel,
                        isAdmin = true
                    )
                }
                is AdminScreen.CreateElection -> {
                    CreateElectionScreen(
                        viewModel = viewModel,
                        repository = repository,
                        vettingElectionId = screen.vettingElectionId,
                        onElectionCreated = { currentAdminScreen = AdminScreen.Dashboard },
                        onCancel = { currentAdminScreen = AdminScreen.Dashboard }
                    )
                }
                is AdminScreen.Results -> {
                    ResultsDashboardScreen(
                        viewModel = viewModel,
                        electionId = screen.electionId,
                    )
                }
            }
        }

        AdminSidebar(
            activeTab = when (currentAdminScreen) {
                is AdminScreen.Dashboard -> "Stages"
                is AdminScreen.VettingSetup, is AdminScreen.CreateElection -> "Vetting"
                is AdminScreen.CandidateApproval -> "Candidates"
                is AdminScreen.ElectionControl -> "Cycle"
                is AdminScreen.Help -> "Guide"
                is AdminScreen.Inbox -> "Inbox"
                else -> "Stages"
            },
            onNavigateStages = { currentAdminScreen = AdminScreen.Dashboard },
            onNavigateVetting = { currentAdminScreen = AdminScreen.VettingSetup },
            onNavigateCandidates = { currentAdminScreen = AdminScreen.CandidateApproval },
            onNavigateCycle = { currentAdminScreen = AdminScreen.ElectionControl },
            onNavigateGuide = { currentAdminScreen = AdminScreen.Help },
            onNavigateInbox = { currentAdminScreen = AdminScreen.Inbox },
            onLogout = onLogout
        )
    }
}
