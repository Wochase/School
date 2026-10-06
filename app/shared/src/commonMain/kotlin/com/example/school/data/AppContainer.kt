package com.example.e_voting.data

import com.example.e_voting.network.ElectionRepository as NetworkElectionRepository
import com.example.e_voting.network.KtorApiClient
import com.example.e_voting.network.MockElectionRepository

object AppContainer {
    // Set true to use the live middleware; false keeps data local to this app session.
    private const val USE_LIVE_BACKEND = false
    private const val LIVE_BACKEND_BASE_URL = "http://127.0.0.1:8000/api"

    val repository: NetworkElectionRepository by lazy {
        if (USE_LIVE_BACKEND) {
            KtorApiClient(baseUrl = LIVE_BACKEND_BASE_URL)
        } else {
            MockElectionRepository()
        }
    }
}
