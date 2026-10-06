package com.example.e_voting.ui.inbox

import com.example.e_voting.data.model.AuditLogEntry
import com.example.e_voting.data.model.Election
import com.example.e_voting.data.model.PendingCandidate
import com.example.e_voting.network.ElectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InboxNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val approved: Boolean = false,
)

data class InboxState(
    val notifications: List<InboxNotification> = emptyList(),
    val readNotificationIds: Set<String> = emptySet(),
    val error: String? = null,
) {
    val unreadCount: Int
        get() = notifications.count { it.id !in readNotificationIds }
}

class InboxViewModel(
    private val repository: ElectionRepository,
    private val isAdmin: Boolean,
    private val studentId: String,
) {
    private val _state = MutableStateFlow(InboxState())
    val state: StateFlow<InboxState> = _state.asStateFlow()

    suspend fun refresh() {
        try {
            val elections = repository.getElections()
            val applications = repository.getPendingCandidates()
            val auditLogs = repository.auditLogs.value
            val notifications = if (isAdmin) {
                adminNotifications(applications, auditLogs)
            } else {
                studentNotifications(elections, applications, auditLogs)
            }
            _state.value = _state.value.copy(notifications = notifications, error = null)
        } catch (_: Exception) {
            _state.value = _state.value.copy(error = "Could not refresh inbox notifications.")
        }
    }

    fun markRead(notificationId: String) {
        _state.value = _state.value.copy(readNotificationIds = _state.value.readNotificationIds + notificationId)
    }

    private fun adminNotifications(
        applications: List<PendingCandidate>,
        auditLogs: List<AuditLogEntry>,
    ) = buildList {
        applications.filter { it.status == "Pending Verification" }.forEach { application ->
            add(
                InboxNotification(
                    id = "application:${application.id}:${application.status}",
                    title = "Application to review: ${application.fullName}",
                    message = "${application.studentId} applied for ${application.positionName}. Suitability statement: ${application.manifesto}",
                    timestamp = "Needs review",
                ),
            )
        }
        auditLogs.asReversed().forEach { add(it.toNotification()) }
    }

    private fun studentNotifications(
        elections: List<Election>,
        applications: List<PendingCandidate>,
        auditLogs: List<AuditLogEntry>,
    ) = buildList {
        elections.filter { it.status in setOf("Vetting Open", "Vetting Closed", "Scheduled", "Open", "Closed", "Results Published") }
            .forEach { election ->
            val message = when (election.status) {
                "Vetting Open" -> election.vettingAnnouncement.ifBlank { election.description }
                "Vetting Closed" -> "Applications for ${election.name} are now closed."
                "Scheduled" -> "${election.name} is scheduled to run from ${election.startDatetime} to ${election.endDatetime}."
                "Open" -> "Voting is now open for ${election.name}."
                "Closed" -> "Voting has closed for ${election.name}."
                else -> "Results for ${election.name} have been published."
            }
            add(
                InboxNotification(
                    id = "election:${election.electionId}:${election.status}",
                    title = when (election.status) {
                        "Vetting Open" -> "Applications open: ${election.name}"
                        "Vetting Closed" -> "Applications closed: ${election.name}"
                        "Scheduled" -> "Election scheduled: ${election.name}"
                        "Open" -> "Voting is open: ${election.name}"
                        "Closed" -> "Voting closed: ${election.name}"
                        else -> "Results published: ${election.name}"
                    },
                    message = message,
                    timestamp = "Election update · ${election.status}",
                ),
            )
        }
        applications.filter { it.studentId == studentId }.forEach { application ->
            val approved = application.status == "Approved"
            val message = when (application.status) {
                "Approved" -> "Your application for ${application.positionName} was approved. You will be included on the ballot."
                "Rejected" -> "Your application for ${application.positionName} was not approved."
                else -> "Your application for ${application.positionName} is awaiting an administrator's decision."
            }
            add(
                InboxNotification(
                    id = "application:${application.id}:${application.status}",
                    title = if (approved) "You are approved: ${application.positionName}" else "Application ${application.status.lowercase()}",
                    message = message,
                    timestamp = "Status: ${application.status}",
                    approved = approved,
                ),
            )
        }
        auditLogs.filter { it.actorId == studentId }.asReversed().forEach { add(it.toNotification()) }
    }
}

private fun AuditLogEntry.toNotification() = InboxNotification(
    id = "audit:$logId",
    title = action,
    message = "$actorRole · $actorId",
    timestamp = timestamp,
)
