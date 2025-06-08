package com.apptimistiq.android.fitstreak.main.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashViewModel : ViewModel() {

    // StateFlow to track if authentication state is initialized
    private val _isAuthStateInitialized = MutableStateFlow(false)
    val isAuthStateInitialized: StateFlow<Boolean> = _isAuthStateInitialized.asStateFlow()

    // Function to set auth state as initialized (will dismiss splash screen)
    fun setAuthInitialized() {
        _isAuthStateInitialized.value = true
    }
}