package com.apptimistiq.android.fitstreak.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.domain.MainUIState
import com.apptimistiq.android.fitstreak.main.home.PermissionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    private val _permissionEvent = MutableStateFlow<PermissionEvent?>(null)
    val permissionEvent = _permissionEvent.asStateFlow()

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        _uiState.update { it.copy(isActivityPermissionGranted = isGranted) }

        if (isGranted) {
            _permissionEvent.value = PermissionEvent.NavigateToDailyProgress
        } else {
            if (shouldShowRationale) {
                _permissionEvent.value = PermissionEvent.ShowPermissionRationale
            }
        }
    }

    fun onPermissionEventHandled() {
        _permissionEvent.value = null
    }

    fun setBottomNavVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(bottomNavVisible = isVisible) }
    }
}
