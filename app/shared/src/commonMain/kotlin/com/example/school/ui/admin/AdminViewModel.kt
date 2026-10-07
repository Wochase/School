package com.example.school.ui.admin

import com.example.school.data.model.Candidate
import com.example.school.data.model.Election
import com.example.school.data.model.ElectionResults
import com.example.school.data.model.Position
import com.example.school.network.ElectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUiState {
    object Idle : AdminUiState
    object Loading : AdminUiState
    data class DashboardLoaded(
        val elections: List<Election>,
        val activeResults: ElectionResults? = null,
        val pendingApplicationCount: Int = 0,
    ) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

class AdminViewModel(private val repository: ElectionRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _adminState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val adminState: StateFlow<AdminUiState> = _adminState.asStateFlow()

    fun loadAdminDashboard() {
        scope.launch {
            _adminState.value = AdminUiState.Loading
            try {
                val elections = repository.getElections()
                val pendingCount = repository.getPendingCandidates().count { it.status == "Pending Verification" }
                _adminState.value = AdminUiState.DashboardLoaded(
                    elections = elections,
                    pendingApplicationCount = pendingCount,
                )
            } catch (_: Exception) {
                _adminState.value = AdminUiState.Error("Failed to load admin elections registry.")
            }
        }
    }

    fun fetchElectionResults(electionId: String) {
        val current = _adminState.value
        if (current is AdminUiState.DashboardLoaded) {
            scope.launch {
                try {
                    val results = repository.getResults(electionId)
                    _adminState.value = current.copy(activeResults = results)
                } catch (_: Exception) {
                    _adminState.value = AdminUiState.Error("Unable to load election results.")
                }
            }
        }
    }

    fun createVettingStage(
        title: String,
        academicYear: String,
        description: String,
        announcement: String,
        roles: List<Pair<String, String>>,
        onSuccess: () -> Unit,
    ) {
        scope.launch {
            _adminState.value = AdminUiState.Loading
            try {
                val vettingElection = Election(
                    electionId = "elec-${(100..999).random()}",
                    name = title,
                    description = description,
                    academicYear = academicYear,
                    startDatetime = "",
                    endDatetime = "",
                    status = "Vetting Open",
                    vettingAnnouncement = announcement,
                    positions = roles.mapIndexed { index, role ->
                        Position(
                            positionId = "pos_${index + 1}",
                            positionName = role.first,
                            description = role.second,
                            maxSelections = 1,
                            minSelections = 1,
                        )
                    },
                )
                check(repository.createElection(vettingElection)) { "The vetting stage could not be created." }
                loadAdminDashboard()
                onSuccess()
            } catch (_: Exception) {
                _adminState.value = AdminUiState.Error("Failed to create election.")
            }
        }
    }

    fun closeVettingStage(electionId: String) {
        scope.launch {
            try {
                val election = repository.getElections().find { it.electionId == electionId }
                    ?: error("Election not found.")
                check(election.status == "Vetting Open") { "This vetting stage is no longer open." }
                check(repository.updateElection(election.copy(status = "Vetting Closed"))) {
                    "The vetting stage could not be closed."
                }
                loadAdminDashboard()
            } catch (_: Exception) {
                _adminState.value = AdminUiState.Error("Failed to close the vetting stage.")
            }
        }
    }

    fun startElectionEarly(electionId: String) {
        scope.launch {
            try {
                val election = repository.getElections().find { it.electionId == electionId }
                    ?: error("Election not found.")
                check(election.status == "Scheduled") {
                    "Only a scheduled election can be started early."
                }
                check(repository.updateElection(election.copy(status = "Open"))) {
                    "The election could not be started."
                }
                loadAdminDashboard()
            } catch (_: Exception) {
                _adminState.value = AdminUiState.Error("Could not start the scheduled election early.")
            }
        }
    }

    fun createElection(
        vettingElectionId: String,
        startDatetime: String,
        endDatetime: String,
        onSuccess: () -> Unit,
    ) {
        scope.launch {
            _adminState.value = AdminUiState.Loading
            try {
                val timestampPattern = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:00Z")
                require(timestampPattern.matches(startDatetime) && timestampPattern.matches(endDatetime))
                require(startDatetime < endDatetime) { "Voting end must be later than voting start." }
                val vettingElection = repository.getElections().find { it.electionId == vettingElectionId }
                    ?: error("Election not found.")
                check(vettingElection.status == "Vetting Closed") {
                    "Close the vetting stage before setting up the election."
                }
                val approved = repository.getPendingCandidates().filter {
                    it.electionId == vettingElectionId && it.status == "Approved"
                }
                val ballotPositions = vettingElection.positions.mapNotNull { position ->
                    val candidates = approved.filter { it.positionName == position.positionName }.map { applicant ->
                        Candidate(
                            candidateId = applicant.id,
                            studentId = applicant.studentId,
                            fullName = applicant.fullName,
                            manifesto = applicant.manifesto,
                            slogan = applicant.slogan,
                            status = "Approved",
                        )
                    }
                    if (candidates.isEmpty()) null else position.copy(candidates = candidates)
                }
                check(ballotPositions.isNotEmpty()) { "Approve at least one candidate before election setup." }
                val election = vettingElection.copy(
                    startDatetime = startDatetime,
                    endDatetime = endDatetime,
                    status = "Scheduled",
                    positions = ballotPositions,
                )
                check(repository.updateElection(election)) { "The election could not be set up." }
                loadAdminDashboard()
                onSuccess()
            } catch (_: Exception) {
                _adminState.value = AdminUiState.Error("Failed to set up the election from approved candidates.")
            }
        }
    }
}
