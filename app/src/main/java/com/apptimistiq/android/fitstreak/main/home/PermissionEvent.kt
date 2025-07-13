package com.apptimistiq.android.fitstreak.main.home

sealed interface PermissionEvent {
    data object NavigateToDailyProgress : PermissionEvent
    data object ShowPermissionRationale : PermissionEvent
}
