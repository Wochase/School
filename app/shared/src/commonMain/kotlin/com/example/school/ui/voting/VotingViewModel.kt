package com.example.e_voting.ui.voting

import com.example.e_voting.data.model.Election
import com.example.e_voting.data.model.VoteSubmission
import com.example.e_voting.data.model.VoterEligibility
import com.example.e_voting.network.ElectionRepository
import com.example.e_voting.network.MockElectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VotingUiState {
    object Idle : VotingUiState
    object Loading : VotingUiState
    data class BallotLoaded(
        val election: Election,
        val eligibility: VoterEligibility,
        val selections: Map<String, String> = emptyMap(),
        val showReviewModal: Boolean = false,
    ) : VotingUiState
    data class Submitted(val confirmationCode: String) : VotingUiState
    data class Error(val message: String) : VotingUiState
}

class VotingViewModel(private val repository: ElectionRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow<VotingUiState>(VotingUiState.Idle)
    val uiState: StateFlow<VotingUiState> = _uiState.asStateFlow()

    fun loadBallotForStudent(electionId: String, studentId: String) {
        scope.launch {
            _uiState.value = VotingUiState.Loading
            try {
                val eligibility = repository.getEligibility(electionId, studentId)
                if (!eligibility.isEligible) {
                    _uiState.value = VotingUiState.Error("You are not registered as eligible for this election.")
                    return@launch
                }
                if (eligibility.hasVoted) {
                    _uiState.value = VotingUiState.Error("You have already submitted a vote for this election.")
                    return@launch
                }
                val election = repository.getActiveElection(electionId)
                _uiState.value = VotingUiState.BallotLoaded(election = election, eligibility = eligibility)
            } catch (_: Exception) {
                _uiState.value = VotingUiState.Error("Failed to fetch ballot details from server.")
            }
        }
    }

    fun selectCandidate(positionId: String, candidateId: String) {
        val current = _uiState.value
        if (current is VotingUiState.BallotLoaded) {
            val updated = current.selections.toMutableMap()
            updated[positionId] = candidateId
            _uiState.value = current.copy(selections = updated)
        }
    }

    fun setReviewModalVisible(visible: Boolean) {
        val current = _uiState.value
        if (current is VotingUiState.BallotLoaded) {
            _uiState.value = current.copy(showReviewModal = visible)
        }
    }

    fun confirmAndSubmitVote(studentId: String) {
        val current = _uiState.value
        if (current is VotingUiState.BallotLoaded) {
            scope.launch {
                val electionId = current.election.electionId
                val selectionsMap = current.selections
                _uiState.value = VotingUiState.Loading
                try {
                    val submission = VoteSubmission(
                        electionId = electionId,
                        votingToken = "one-time-anon-auth-token-${(1000..9999).random()}",
                        selections = selectionsMap,
                    )
                    val response = repository.submitVote(submission)
                    if (response.success) {
                        if (repository is MockElectionRepository) {
                            repository.markVoted(electionId, studentId)
                        }
                        _uiState.value = VotingUiState.Submitted(response.confirmationCode)
                    } else {
                        _uiState.value = VotingUiState.Error(response.message ?: "Vote submission failed.")
                    }
                } catch (_: Exception) {
                    _uiState.value = VotingUiState.Error("Network failure during vote submission.")
                }
            }
        }
    }
}
