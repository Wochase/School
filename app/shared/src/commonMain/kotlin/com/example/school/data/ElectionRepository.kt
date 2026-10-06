package com.example.e_voting.data

import com.example.e_voting.data.model.PendingCandidate

interface ElectionRepository {
    suspend fun login(studentId: String, pass: String): Result<Boolean>
    suspend fun fetchActiveElections(): Result<List<String>>
    suspend fun fetchCandidates(electionId: String): Result<List<PendingCandidate>>
    suspend fun submitVote(electionId: String, candidateId: String, token: String): Result<String>
    suspend fun fetchStudentEligibility(studentId: String): Result<Boolean>
}