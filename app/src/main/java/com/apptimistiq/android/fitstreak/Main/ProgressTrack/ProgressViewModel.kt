package com.apptimistiq.android.fitstreak.Main.ProgressTrack

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.apptimistiq.android.fitstreak.utils.Event

class ProgressViewModel(val app: Application) : AndroidViewModel(app) {

    private val _showPermissionDialog = MutableLiveData<Event<Boolean>>()

    val showPermissionDialog: LiveData<Event<Boolean>>
        get() = _showPermissionDialog

    /**
     * Trigger this method whenever the system decides to show PermissionRationale
     * and a dialogFragment needs to be appeared
     */

    fun showDialog(show: Boolean) {
        _showPermissionDialog.value = Event(show)
    }


}