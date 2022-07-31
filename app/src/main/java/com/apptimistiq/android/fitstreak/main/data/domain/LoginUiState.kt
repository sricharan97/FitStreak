package com.apptimistiq.android.fitstreak.main.data.domain

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUserLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false
)