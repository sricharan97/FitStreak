package com.apptimistiq.android.fitstreak.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// @Inject tells Dagger how to provide instances of this type
class AuthenticationViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {

    //current userState info
    val userState: StateFlow<UserStateInfo> = dataSource.getCurrentUserState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserStateInfo()
    )

    //keep track of sign in flow
    private val _signInFlowStatus = MutableStateFlow(false)
    val signInFlowStatus: StateFlow<Boolean> = _signInFlowStatus.asStateFlow()


    fun saveGoal(goalType: GoalType, value: Int) {
        viewModelScope.launch {
            dataSource.saveGoal(goalType, value)
        }

    }

    fun saveUserStateInfo(userStateInfo: UserStateInfo) {
        viewModelScope.launch {
            dataSource.saveUserState(userStateInfo)
        }
    }

    fun signInFlowCompleted() {
        _signInFlowStatus.value = true
    }

    fun signInFlowReset() {
        _signInFlowStatus.value = false
    }

}