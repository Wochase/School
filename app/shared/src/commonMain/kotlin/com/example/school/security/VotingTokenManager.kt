package com.example.e_voting.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VotingTokenManager {
    private val _currentVotingToken = MutableStateFlow<String?>(null)
    val currentVotingToken: StateFlow<String?> = _currentVotingToken.asStateFlow()

    fun issueOneTimeToken(studentId: String, electionId: String): String {
        // Generates an in-memory single-use anonymous authorization token
        val token = "anon-token-${studentId.hashCode()}-${electionId.hashCode()}-${(100000..999999).random()}"
        _currentVotingToken.value = token
        return token
    }

    fun consumeToken(): String? {
        val token = _currentVotingToken.value
        _currentVotingToken.value = null // Single-use invalidation
        return token
    }
}