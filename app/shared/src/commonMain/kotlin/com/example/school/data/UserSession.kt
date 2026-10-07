package com.example.school.data

import com.example.school.data.model.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    private val _currentUser = MutableStateFlow<AuthResponse?>(null)
    val currentUser: StateFlow<AuthResponse?> = _currentUser.asStateFlow()

    fun startSession(authResponse: AuthResponse) {
        _currentUser.value = authResponse
    }

    fun clearSession() {
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null
    fun isSystemAdmin(): Boolean = _currentUser.value?.userRole == "System Administrator"
}