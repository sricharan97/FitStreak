package com.apptimistiq.android.fitstreak.main.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class UserProfileRepositoryTest {

    // Subject under test
    private lateinit var repository: UserProfileRepository

    // Fake dependencies
    private lateinit var fakeDataStore: FakePreferencesDataStore

    private val testDispatcher = StandardTestDispatcher()

    private object TestPreferenceKeys {
        val STEP_GOAL = intPreferencesKey("steps_goal")
        val WATER_GLASS_GOAL = intPreferencesKey("water_glass_goal")
        val EXERCISE_GOAL = intPreferencesKey("exercise_goal")
        val SLEEP_GOAL = intPreferencesKey("sleep_goal")
        val HEIGHT_INFO = intPreferencesKey("userHeight")
        val WEIGHT_INFO = intPreferencesKey("userWeight")
        val DIET_SELECTION = stringPreferencesKey("diet_selection")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_LOGGED_IN = booleanPreferencesKey("user_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ONBOARDED = booleanPreferencesKey("user_onboarded")
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDataStore = FakePreferencesDataStore()
        repository = UserProfileRepository(fakeDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun goalPreferences_emitsCorrectData() = runTest {
        // Given
        val testGoals = GoalPreferences(10000, 8, 8, 500)
        fakeDataStore.edit {
            it[TestPreferenceKeys.STEP_GOAL] = testGoals.stepGoal
            it[TestPreferenceKeys.WATER_GLASS_GOAL] = testGoals.waterGlassGoal
            it[TestPreferenceKeys.SLEEP_GOAL] = testGoals.sleepGoal
            it[TestPreferenceKeys.EXERCISE_GOAL] = testGoals.exerciseGoal
        }

        // When
        val result = repository.goalPreferences.first()

        // Then
        assertEquals(testGoals, result)
    }

    @Test
    fun userInfoPreferences_emitsCorrectData() = runTest {
        // Given
        val testUserInfo = UserInfoPreferences(180, 75)
        fakeDataStore.edit {
            it[TestPreferenceKeys.HEIGHT_INFO] = testUserInfo.height
            it[TestPreferenceKeys.WEIGHT_INFO] = testUserInfo.weight
        }

        // When
        val result = repository.userInfoPreferences.first()

        // Then
        assertEquals(testUserInfo, result)
    }

    @Test
    fun userStateInfo_emitsCorrectData() = runTest {
        // Given
        val testUserState = UserStateInfo("uid1", "Test", true, true)
        fakeDataStore.edit {
            it[TestPreferenceKeys.USER_UID] = testUserState.uid
            it[TestPreferenceKeys.USER_NAME] = testUserState.userName
            it[TestPreferenceKeys.USER_LOGGED_IN] = testUserState.isUserLoggedIn
            it[TestPreferenceKeys.USER_ONBOARDED] = testUserState.isOnboarded
        }

        // When
        val result = repository.userStateInfo.first()

        // Then
        assertEquals(testUserState, result)
    }

    @Test
    fun updateStepGoal_updatesValueInDataStore() = runTest {
        // When
        repository.updateStepGoal(12000)

        // Then
        val result = repository.stepsGoal.first()
        assertEquals(12000, result)
    }

    @Test
    fun updateUserStateInfo_updatesAllValuesInDataStore() = runTest {
        // Given
        val newUserState = UserStateInfo("uid2", "New User", false, false)

        // When
        repository.updateUserStateInfo(newUserState)

        // Then
        val result = repository.userStateInfo.first()
        assertEquals(newUserState, result)
    }

    @Test
    fun resetOnboardingAndGoalData_resetsToDefaultValues() = runTest {
        // Given: Set some non-default values first
        repository.updateStepGoal(5000)
        repository.updateUserHeight(190)
        repository.updateDietSelection("Keto")

        // When
        repository.resetOnboardingAndGoalData()

        // Then
        assertEquals(0, repository.stepsGoal.first())
        assertEquals(168, repository.heightInfo.first()) // Default height
        assertEquals(60, repository.weightInfo.first()) // Default weight
        assertEquals("Vegetarian", repository.dietSelection.first()) // Default diet
    }

    @Test(expected = IOException::class)
    fun updateStepGoal_whenIOExceptionOccurs_propagatesException() = runTest {
        // Given
        fakeDataStore.setShouldThrowOnUpdate(true)

        // When
        repository.updateStepGoal(100)
    }

    @Test
    fun goalPreferences_whenIOExceptionOnRead_emitsEmptyPreferences() = runTest {
        // Given
        fakeDataStore.setShouldThrowOnRead(IOException("Read error"))

        // When
        val result = repository.goalPreferences.first()

        // Then
        assertEquals(GoalPreferences(0, 0, 0, 0), result)
    }

    @Test(expected = RuntimeException::class)
    fun goalPreferences_whenOtherExceptionOnRead_propagatesException() = runTest {
        // Given
        fakeDataStore.setShouldThrowOnRead(RuntimeException("Other error"))

        // When
        repository.goalPreferences.first()
    }
}