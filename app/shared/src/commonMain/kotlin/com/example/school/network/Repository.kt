package com.example.e_voting.network

import com.example.e_voting.data.model.AuthResponse
import com.example.e_voting.data.model.CandidateResult
import com.example.e_voting.data.model.Election
import com.example.e_voting.data.model.ElectionResults
import com.example.e_voting.data.model.LoginRequest
import com.example.e_voting.data.model.PendingCandidate
import com.example.e_voting.data.model.Position
import com.example.e_voting.data.model.PositionResult
import com.example.e_voting.data.model.VoteResponse
import com.example.e_voting.data.model.VoteSubmission
import com.example.e_voting.data.model.VoterEligibility
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import com.example.e_voting.data.model.AuditLogEntry

@Serializable
private data class AnonymousVoteTally(
    val electionId: String,
    val positionId: String,
    val candidateId: String,
    val count: Int,
)

interface ElectionRepository {
    val auditLogs: StateFlow<List<AuditLogEntry>>
    suspend fun login(credentials: LoginRequest): AuthResponse
    suspend fun getElections(): List<Election>
    suspend fun getActiveElection(electionId: String): Election
    suspend fun createElection(election: Election): Boolean
    suspend fun updateElection(election: Election): Boolean
    suspend fun getEligibility(electionId: String, studentId: String): VoterEligibility
    suspend fun submitVote(vote: VoteSubmission): VoteResponse
    suspend fun getResults(electionId: String): ElectionResults
    suspend fun getPendingCandidates(): List<PendingCandidate>
    suspend fun submitCandidateApplication(candidate: PendingCandidate)
    suspend fun updateCandidateStatus(candidateId: String, status: String)
}

// 1. Production Ktor Client connecting to Python/FastAPI or Django backend
class KtorApiClient(private val baseUrl: String = "http://localhost:8000/api") : ElectionRepository {
    private var token: String? = null
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    override val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    fun setAuthToken(newToken: String) {
        this.token = newToken
    }

    override suspend fun login(credentials: LoginRequest): AuthResponse {
        val response: AuthResponse = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(credentials)
        }.body()
        setAuthToken(response.accessToken)
        return response
    }

    override suspend fun getElections(): List<Election> {
        return client.get("$baseUrl/elections") {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }.body()
    }

    override suspend fun getActiveElection(electionId: String): Election {
        return client.get("$baseUrl/elections/$electionId/ballot") {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }.body()
    }

    override suspend fun createElection(election: Election): Boolean {
        val response: VoteResponse = client.post("$baseUrl/elections") {
            contentType(ContentType.Application.Json)
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            setBody(election)
        }.body()
        return response.success
    }

    override suspend fun updateElection(election: Election): Boolean {
        val response: VoteResponse = client.put("$baseUrl/elections/${election.electionId}") {
            contentType(ContentType.Application.Json)
            token?.let { header(HttpHeaders.Authorization, "******") }
            setBody(election)
        }.body()
        return response.success
    }

    override suspend fun getEligibility(electionId: String, studentId: String): VoterEligibility {
        return client.get("$baseUrl/elections/$electionId/eligibility/$studentId") {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }.body()
    }

    override suspend fun submitVote(vote: VoteSubmission): VoteResponse {
        return client.post("$baseUrl/elections/${vote.electionId}/vote") {
            contentType(ContentType.Application.Json)
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            setBody(vote)
        }.body()
    }

    override suspend fun getResults(electionId: String): ElectionResults {
        return client.get("$baseUrl/elections/$electionId/results") {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }.body()
    }

    override suspend fun getPendingCandidates(): List<PendingCandidate> {
        return client.get("$baseUrl/candidates/pending") {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }.body()
    }

    override suspend fun submitCandidateApplication(candidate: PendingCandidate) {
        client.post("$baseUrl/candidates/apply") {
            contentType(ContentType.Application.Json)
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            setBody(candidate)
        }
    }

    override suspend fun updateCandidateStatus(candidateId: String, status: String) {
        client.patch("$baseUrl/candidates/$candidateId/status") {
            contentType(ContentType.Application.Json)
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            setBody(mapOf("status" to status))
        }
    }
}

// 2. Full Mock Repository for Immediate UI Testing without Backend dependencies
class MockElectionRepository : ElectionRepository {
    private var loggedInRole = "Student"
    private val votedMap = mutableMapOf<String, Boolean>()
    private val electionsList = mutableListOf<Election>()
    private val pendingCandidatesList = mutableListOf<PendingCandidate>()
    private val voteTallies = mutableListOf<AnonymousVoteTally>()
    private val ballotCounts = mutableMapOf<String, Int>()
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    override val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private fun recordAudit(actorRole: String, actorId: String, action: String) {
        val nextId = (_auditLogs.value.lastOrNull()?.logId ?: 0L) + 1L
        _auditLogs.value = _auditLogs.value + AuditLogEntry(
            logId = nextId,
            timestamp = Clock.System.now().toString(),
            actorRole = actorRole,
            actorId = actorId,
            action = action,
        )
    }

    override suspend fun login(credentials: LoginRequest): AuthResponse {
        delay(600)
        loggedInRole = if (credentials.username.contains("admin", ignoreCase = true)) "System Administrator" else "Student"
        recordAudit(loggedInRole, credentials.username, "Signed in")
        return AuthResponse(
            accessToken = "mock-jwt-token-xyz-12345",
            userRole = loggedInRole,
            studentId = if (loggedInRole == "Student") credentials.username else null
        )
    }

    override suspend fun getElections(): List<Election> {
        delay(400)
        return electionsList.toList()
    }

    override suspend fun getActiveElection(electionId: String): Election {
        delay(600)
        return electionsList.find { it.electionId == electionId } ?: electionsList.first()
    }

    override suspend fun createElection(election: Election): Boolean {
        delay(400)
        electionsList.removeAll { it.electionId == election.electionId }
        electionsList.add(election)
        recordAudit("Admin", "Administrator", "Opened candidate vetting for ${election.name}")
        return true
    }

    override suspend fun updateElection(election: Election): Boolean {
        delay(300)
        val index = electionsList.indexOfFirst { it.electionId == election.electionId }
        if (index < 0) return false
        electionsList[index] = election
        recordAudit("Admin", "Administrator", "Updated ${election.name} status to ${election.status}")
        return true
    }

    override suspend fun getEligibility(electionId: String, studentId: String): VoterEligibility {
        delay(300)
        val hasVoted = votedMap["$electionId:$studentId"] == true
        return VoterEligibility(
            studentId = studentId,
            electionId = electionId,
            isEligible = true,
            hasVoted = hasVoted
        )
    }

    override suspend fun submitVote(vote: VoteSubmission): VoteResponse {
        delay(1200) // Simulate atomic vote processing
        val election = electionsList.find { it.electionId == vote.electionId }
            ?: return VoteResponse(success = false, confirmationCode = "", message = "Election not found.")
        if (election.status != "Open") {
            return VoteResponse(success = false, confirmationCode = "", message = "Voting is not open.")
        }
        require(vote.selections.all { (positionId, candidateId) ->
            election.positions.any { position ->
                position.positionId == positionId && position.candidates.any { it.candidateId == candidateId }
            }
        }) { "Vote contains a candidate that is not on this election ballot." }
        vote.selections.forEach { (positionId, candidateId) ->
            val tallyIndex = voteTallies.indexOfFirst {
                it.electionId == vote.electionId &&
                    it.positionId == positionId &&
                    it.candidateId == candidateId
            }
            if (tallyIndex >= 0) {
                voteTallies[tallyIndex] = voteTallies[tallyIndex].copy(count = voteTallies[tallyIndex].count + 1)
            } else {
                voteTallies.add(AnonymousVoteTally(vote.electionId, positionId, candidateId, 1))
            }
        }
        ballotCounts[vote.electionId] = (ballotCounts[vote.electionId] ?: 0) + 1
        recordAudit("System", "Voting service", "Recorded vote for election ${vote.electionId}")
        return VoteResponse(
            success = true,
            confirmationCode = "REC-VOTE-${(100000..999999).random()}"
        )
    }

    fun markVoted(electionId: String, studentId: String) {
        votedMap["$electionId:$studentId"] = true
    }

    override suspend fun getResults(electionId: String): ElectionResults {
        delay(500)
        val election = electionsList.find { it.electionId == electionId }
            ?: error("Election '$electionId' was not found.")
        val positionResults = election.positions.map { pos ->
            val candidateVotes = pos.candidates.map { candidate ->
                candidate to (voteTallies.find {
                    it.electionId == electionId &&
                        it.positionId == pos.positionId &&
                        it.candidateId == candidate.candidateId
                }?.count ?: 0)
            }
            val totalVotesForPos = candidateVotes.sumOf { it.second }
            val candidateResults = candidateVotes.map { (cand, votes) ->
                val percentage = if (totalVotesForPos > 0) (votes.toDouble() / totalVotesForPos) * 100.0 else 0.0
                CandidateResult(
                    candidateId = cand.candidateId,
                    candidateName = cand.fullName,
                    voteCount = votes,
                    percentage = percentage
                )
            }
            PositionResult(
                positionId = pos.positionId,
                positionName = pos.positionName,
                results = candidateResults
            )
        }
        return ElectionResults(
            electionId = electionId,
            totalEligibleVoters = 0,
            totalVotesCast = ballotCounts[electionId] ?: 0,
            turnoutPercentage = 0.0,
            positionResults = positionResults
        )
    }

    override suspend fun getPendingCandidates(): List<PendingCandidate> {
        delay(300)
        return pendingCandidatesList.toList()
    }

    override suspend fun submitCandidateApplication(candidate: PendingCandidate) {
        delay(400)
        val election = electionsList.find { it.electionId == candidate.electionId }
        require(election?.status == "Vetting Open") { "Candidate applications are closed for this election." }
        pendingCandidatesList.add(candidate)
        recordAudit("Student", candidate.studentId, "Applied for ${candidate.positionName} in ${election.name}")
    }

    override suspend fun updateCandidateStatus(candidateId: String, status: String) {
        delay(200)
        val candidate = pendingCandidatesList.find { it.id == candidateId } ?: return
        candidate.status = status
        recordAudit("Admin", "Administrator", "$status candidate application for ${candidate.fullName}")
    }
}
