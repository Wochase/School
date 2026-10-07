package com.example.school.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- AUTHENTICATION MODELS ---

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("user_role") val userRole: String, // "System Administrator", "Election Officer", "Student", "Observer"
    @SerialName("student_id") val studentId: String? = null
)

// --- ELECTION & CANDIDATE MODELS ---

@Serializable
data class Candidate(
    @SerialName("candidate_id") val candidateId: String,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("full_name") val fullName: String,
    val manifesto: String,
    @SerialName("photograph_url") val photographUrl: String? = null,
    val slogan: String? = null,
    val status: String = "Approved" // "Pending Verification", "Approved", "Rejected", "Withdrawn"
)

@Serializable
data class Position(
    @SerialName("position_id") val positionId: String,
    @SerialName("position_name") val positionName: String,
    val description: String? = null,
    @SerialName("maximum_selections") val maxSelections: Int = 1,
    @SerialName("minimum_selections") val minSelections: Int = 1,
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Election(
    @SerialName("election_id") val electionId: String,
    val name: String,
    val description: String,
    @SerialName("academic_year") val academicYear: String,
    @SerialName("start_datetime") val startDatetime: String,
    @SerialName("end_datetime") val endDatetime: String,
    val status: String, // Vetting Open/Closed, Scheduled, Open, Closed, Results Published, Archived
    val positions: List<Position> = emptyList(),
    @SerialName("vetting_announcement") val vettingAnnouncement: String = ""
)

// --- VOTING & PRIVACY MODELS ---

@Serializable
data class VoterEligibility(
    @SerialName("student_id") val studentId: String,
    @SerialName("election_id") val electionId: String,
    @SerialName("is_eligible") val isEligible: Boolean,
    @SerialName("has_voted") val hasVoted: Boolean
)

@Serializable
data class VoteSubmission(
    @SerialName("election_id") val electionId: String,
    @SerialName("voting_token") val votingToken: String, // One-time anonymous authorization token
    val selections: Map<String, String> // Key: positionId, Value: candidateId
)

@Serializable
data class VoteResponse(
    val success: Boolean,
    @SerialName("confirmation_code") val confirmationCode: String,
    val message: String? = null
)

// --- ADMIN & REPORTING MODELS ---

@Serializable
data class PendingCandidate(
    @SerialName("candidate_app_id") val id: String,
    @SerialName("election_id") val electionId: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("class_group") val classGroup: String,
    @SerialName("position_name") val positionName: String,
    val slogan: String,
    val manifesto: String, // Suitability statement
    var status: String, // "Pending Verification", "Approved", "Rejected"
)

@Serializable
data class CandidateResult(
    @SerialName("candidate_id") val candidateId: String,
    @SerialName("candidate_name") val candidateName: String,
    @SerialName("vote_count") val voteCount: Int,
    val percentage: Double
)

@Serializable
data class PositionResult(
    @SerialName("position_id") val positionId: String,
    @SerialName("position_name") val positionName: String,
    val results: List<CandidateResult>
)

@Serializable
data class ElectionResults(
    @SerialName("election_id") val electionId: String,
    @SerialName("total_eligible_voters") val totalEligibleVoters: Int,
    @SerialName("total_votes_cast") val totalVotesCast: Int,
    @SerialName("turnout_percentage") val turnoutPercentage: Double,
    @SerialName("position_results") val positionResults: List<PositionResult>
)

@Serializable
data class AuditLogEntry(
    val logId: Long,
    val timestamp: String,
    val actorRole: String,
    val actorId: String,
    val action: String,
    val status: String = "Success",
    val ipAddress: String = "Local session",
)