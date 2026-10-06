package com.example.e_voting.data

import com.example.e_voting.data.model.PendingCandidate
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AuthRequest(val studentId: String, val password: String)

@Serializable
data class AuthResponse(val success: Boolean, val message: String? = null)

@Serializable
data class VoteSubmission(val electionId: String, val candidateId: String, val token: String)

@Serializable
data class VoteResponse(val success: Boolean, val receiptCode: String? = null)

class ApiElectionRepository(
    private val baseUrl: String = "http://127.0.0.1:8000/api"
) : ElectionRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    override suspend fun login(studentId: String, pass: String): Result<Boolean> = runCatching {
        val response: AuthResponse = client.post("$baseUrl/accounts/login/") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(studentId, pass))
        }.body()
        response.success
    }

    override suspend fun fetchActiveElections(): Result<List<String>> = runCatching {
        client.get("$baseUrl/elections/").body()
    }

    override suspend fun fetchCandidates(electionId: String): Result<List<PendingCandidate>> = runCatching {
        client.get("$baseUrl/elections/$electionId/candidates/").body()
    }

    override suspend fun submitVote(electionId: String, candidateId: String, token: String): Result<String> = runCatching {
        val response: VoteResponse = client.post("$baseUrl/elections/vote/") {
            contentType(ContentType.Application.Json)
            setBody(VoteSubmission(electionId, candidateId, token))
        }.body()
        response.receiptCode ?: throw Exception("Vote submission failed")
    }

    override suspend fun fetchStudentEligibility(studentId: String): Result<Boolean> = runCatching {
        val response: AuthResponse = client.get("$baseUrl/accounts/eligibility/$studentId/").body()
        response.success
    }
}