package com.example.school.ui.auth

import com.example.school.data.model.AuthResponse
import com.example.school.data.model.LoginRequest
import com.example.school.network.ElectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Authenticating : AuthUiState
    data class Authenticated(val response: AuthResponse) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val repository: ElectionRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun login(usernameInput: String, passwordInput: String) {
        if (usernameInput.isBlank() || passwordInput.isBlank()) {
            _authState.value = AuthUiState.Error("Please enter both Student ID and password.")
            return
        }
        scope.launch {
            _authState.value = AuthUiState.Authenticating
            try {
                val response = repository.login(LoginRequest(usernameInput, passwordInput))
                _authState.value = AuthUiState.Authenticated(response)
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Invalid credentials or network error.")
            }
        }
    }

    fun logout() {
        _authState.value = AuthUiState.Idle
    }
}
