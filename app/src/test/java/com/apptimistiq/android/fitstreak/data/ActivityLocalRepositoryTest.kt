package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.*
import com.apptimistiq.android.fitstreak.main.data.test.FakeActivityDao
import com.apptimistiq.android.fitstreak.main.data.test.FakeUserProfileDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@ExperimentalCoroutinesApi
class ActivityLocalRepositoryTest {
    // Subject under test
    private lateinit var repository: ActivityLocalRepository

    // Fake dependencies
    private lateinit var fakeActivityDao: FakeActivityDao
    private lateinit var fakeUserProfileDataSource: FakeUserProfileDataSource

    // Test dispatcher
    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testTimestamp = System.currentTimeMillis()

    private val testActivity = Activity(
        id = 1L,
        dateOfActivity = testTimestamp,
        waterGlasses = 5,
        sleepHours = 8,
        exerciseCalories = 300,
        steps = 7500
    )

    private val testGoalPreferences = GoalPreferences(
        stepGoal = 10000,
        waterGlassGoal = 8,
        sleepGoal = 8,
        exerciseGoal = 500
    )

    private val testUserInfoPreferences = UserInfoPreferences(
        height = 175,
        weight = 70
    )

    private val testUserStateInfo = UserStateInfo(
        uid = "test-user",
        userName = "Test User",
        isUserLoggedIn = true,
        isOnboarded = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeActivityDao = FakeActivityDao()
        fakeUserProfileDataSource = FakeUserProfileDataSource()

        repository = ActivityLocalRepository(
            activityDao = fakeActivityDao,
            ioDispatcher = testDispatcher,
            userProfileDataSource = fakeUserProfileDataSource
        )

        setupDefaultTestData()
    }

    private fun setupDefaultTestData() {
        // Set up ActivityDao data
        fakeActivityDao.setCurrentTimeMillis(testTimestamp)
        fakeActivityDao.setActivities(listOf(testActivity))

        // Set up UserProfileDataSource data
        fakeUserProfileDataSource.setGoalPreferences(testGoalPreferences)
        fakeUserProfileDataSource.setUserInfoPreferences(testUserInfoPreferences)
        fakeUserProfileDataSource.setUserStateInfo(testUserStateInfo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getTodayActivity_returnsCorrectActivityItems() = runTest {
        // When: Getting today's activity
        val result = repository.getTodayActivity().first()

        // Then: Should return correct values with goals applied
        assertNotNull(result)
        result?.let {
            assertEquals(4, it.size)

            val waterActivity = it.find { item -> item.dataType == ActivityType.WATER }
            assertNotNull(waterActivity)
            assertEquals(5, waterActivity?.currentReading)
            assertEquals(8, waterActivity?.goalReading)

            val sleepActivity = it.find { item -> item.dataType == ActivityType.SLEEP }
            assertNotNull(sleepActivity)
            assertEquals(8, sleepActivity?.currentReading)
            assertEquals(8, sleepActivity?.goalReading)
        }
    }

    @Test
    fun getWeekActivities_returnsActivitiesFromDao() = runTest {
        // Given: Multiple activities for the week
        val weekActivities = listOf(
            testActivity,
            testActivity.copy(id = 2, dateOfActivity = testTimestamp - 86400000), // Yesterday
            testActivity.copy(id = 3, dateOfActivity = testTimestamp - 172800000) // 2 days ago
        )
        fakeActivityDao.setActivities(weekActivities)

        // When: Getting week activities
        val result = repository.getWeekActivities().first()

        // Then: Should return all activities
        assertEquals(3, result.size)
    }

    @Test
    fun getCurrentActivityVal_returnsCorrectValue() = runTest {
        // When: Getting current value for each activity type
        val waterValue = repository.getCurrentActivityVal(ActivityType.WATER).first()
        val sleepValue = repository.getCurrentActivityVal(ActivityType.SLEEP).first()
        val exerciseValue = repository.getCurrentActivityVal(ActivityType.EXERCISE).first()
        val stepValue = repository.getCurrentActivityVal(ActivityType.STEP).first()

        // Then: Should return correct values
        assertEquals(5, waterValue)
        assertEquals(8, sleepValue)
        assertEquals(300, exerciseValue)
        assertEquals(7500, stepValue)
    }

    @Test
    fun saveActivity_savesNewActivityToDao() = runTest {
        // Given: Clear existing activities
        fakeActivityDao.clearActivities()

        // Given: Activity items to save
        val activityItems = listOf(
            ActivityItemUiState(ActivityType.WATER, 6, 8),
            ActivityItemUiState(ActivityType.SLEEP, 7, 8),
            ActivityItemUiState(ActivityType.EXERCISE, 400, 500),
            ActivityItemUiState(ActivityType.STEP, 8000, 10000)
        )

        // When: Saving activity
        repository.saveActivity(activityItems, testTimestamp)

        // Then: DAO should have the new activity
        val savedActivity = fakeActivityDao.getTodayActivity().first()
        assertEquals(6, savedActivity.waterGlasses)
        assertEquals(7, savedActivity.sleepHours)
        assertEquals(400, savedActivity.exerciseCalories)
        assertEquals(8000, savedActivity.steps)
    }

    @Test
    fun updateActivity_updatesExistingActivity() = runTest {
        // Given: Clear existing activities
        fakeActivityDao.clearActivities()

        // Given: Add a known activity with exercise calories = 300
        val initialActivity = Activity(
            id = 1,
            dateOfActivity = testTimestamp,
            waterGlasses = 3,
            sleepHours = 6,
            exerciseCalories = 300,
            steps = 7500
        )
        fakeActivityDao.addActivity(initialActivity)

        // Given: Activity items to update (only water and sleep)
        val activityItems = listOf(
            ActivityItemUiState(ActivityType.WATER, 7, 8),
            ActivityItemUiState(ActivityType.SLEEP, 9, 8)
        )

        // When: Updating activity
        repository.updateActivity(activityItems, testTimestamp)

        // Then: DAO should have updated values
        val updatedActivity = fakeActivityDao.getTodayActivity().first()
        assertEquals(7, updatedActivity.waterGlasses)
        assertEquals(9, updatedActivity.sleepHours)
        // Other fields should remain unchanged
        assertEquals(300, updatedActivity.exerciseCalories)
        assertEquals(7500, updatedActivity.steps)
    }

    @Test
    fun updateActivity_createsNewActivityIfNotExists() = runTest {
        // Given: Clear existing activities
        fakeActivityDao.clearActivities()

        // Given: Activity items to update for non-existent date
        val newTimestamp = testTimestamp + 86400000 // Tomorrow
        val activityItems = listOf(
            ActivityItemUiState(ActivityType.WATER, 3, 8),
            ActivityItemUiState(ActivityType.SLEEP, 6, 8)
        )

        // When: Updating activity for a new date
        repository.updateActivity(activityItems, newTimestamp)

        // Then: DAO should create a new activity
        assertTrue(fakeActivityDao.activityExistsForDate(newTimestamp))

        // Set timestamp to find new activity
        fakeActivityDao.setCurrentTimeMillis(newTimestamp)
        val newActivity = fakeActivityDao.getTodayActivity().first()
        assertEquals(3, newActivity.waterGlasses)
        assertEquals(6, newActivity.sleepHours)
    }

    @Test
    fun getCurrentUserState_returnsUserStateFromDataSource() = runTest {
        // When: Getting current user state
        val result = repository.getCurrentUserState().first()

        // Then: Should return user state from data source
        assertEquals(testUserStateInfo, result)
    }

    @Test
    fun getCurrentGoals_returnsGoalPreferencesFromDataSource() = runTest {
        // When: Getting current goals
        val result = repository.getCurrentGoals().first()

        // Then: Should return goal preferences from data source
        assertEquals(testGoalPreferences, result)
    }

    @Test
    fun getCurrentUserInfo_returnsUserInfoFromDataSource() = runTest {
        // When: Getting current user info
        val result = repository.getCurrentUserInfo().first()

        // Then: Should return user info from data source
        assertEquals(testUserInfoPreferences, result)
    }

    @Test
    fun getCurrentGoalUserInfo_returnsCorrectValuesForDifferentTypes() = runTest {
        // When: Getting different goal user info types
        val heightValue = repository.getCurrentGoalUserInfo(GoalUserInfo.HEIGHT).first()
        val weightValue = repository.getCurrentGoalUserInfo(GoalUserInfo.WEIGHT).first()
        val waterValue = repository.getCurrentGoalUserInfo(GoalUserInfo.WATER).first()
        val stepsValue = repository.getCurrentGoalUserInfo(GoalUserInfo.STEPS).first()
        val defaultValue = repository.getCurrentGoalUserInfo(GoalUserInfo.DEFAULT).first()

        // Then: Should return correct values
        assertEquals(175, heightValue)
        assertEquals(70, weightValue)
        assertEquals(8, waterValue)
        assertEquals(10000, stepsValue)
        assertEquals(0, defaultValue)
    }

    @Test
    fun saveGoal_updatesGoalInDataSource() = runTest {
        // When: Saving different goals
        repository.saveGoal(GoalType.STEP, 12000)
        repository.saveGoal(GoalType.WATER, 10)

        // Then: Data source should have updated values
        val updatedGoals = fakeUserProfileDataSource.goalPreferences.first()
        assertEquals(12000, updatedGoals.stepGoal)
        assertEquals(10, updatedGoals.waterGlassGoal)
        // Other values should remain unchanged
        assertEquals(8, updatedGoals.sleepGoal)
        assertEquals(500, updatedGoals.exerciseGoal)
    }

    @Test
    fun saveGoalInfo_updatesCorrectValueInDataSource() = runTest {
        // When: Saving different goal info types
        repository.saveGoalInfo(GoalUserInfo.HEIGHT, 180)
        repository.saveGoalInfo(GoalUserInfo.WEIGHT, 75)
        repository.saveGoalInfo(GoalUserInfo.SLEEP, 9)

        // Then: Data source should have updated values
        val updatedUserInfo = fakeUserProfileDataSource.userInfoPreferences.first()
        assertEquals(180, updatedUserInfo.height)
        assertEquals(75, updatedUserInfo.weight)

        val updatedGoals = fakeUserProfileDataSource.goalPreferences.first()
        assertEquals(9, updatedGoals.sleepGoal)
    }

    @Test
    fun saveUserState_updatesStateInDataSource() = runTest {
        // Given: New user state
        val newUserState = UserStateInfo(
            uid = "new-user",
            userName = "New User",
            isUserLoggedIn = false,
            isOnboarded = false
        )

        // When: Saving user state
        repository.saveUserState(newUserState)

        // Then: Data source should have updated state
        val updatedState = fakeUserProfileDataSource.userStateInfo.first()
        assertEquals(newUserState, updatedState)
    }

    @Test
    fun getTodayActivity_whenDaoThrowsException_propagatesError() = runTest {
        // Given: Configure DAO to throw exception (requires adding this capability to FakeActivityDao)
        fakeActivityDao.setShouldThrowException(true)

        // When/Then: The exception should be propagated
        assertThrows(Exception::class.java) {
            runTest {
                repository.getTodayActivity().first()
            }
        }
    }

    @Test
    fun updateActivity_whenDaoThrowsException_propagatesError() = runTest {
        // Given: Configure DAO to throw exception during update
        fakeActivityDao.setShouldThrowExceptionOnUpdate(true)

        // When/Then: The exception should be propagated
        assertThrows(Exception::class.java) {
            runTest {
                repository.updateActivity(
                    listOf(ActivityItemUiState(ActivityType.WATER, 5, 8)),
                    testTimestamp
                )
            }
        }
    }

    @Test
    fun saveActivity_withEmptyList_savesZeroValues() = runTest {
        // Given: Clear existing activities
        fakeActivityDao.clearActivities()

        // When: Save with empty list
        repository.saveActivity(emptyList(), testTimestamp)

        // Then: Should save activity with all zeros
        val savedActivity = fakeActivityDao.getTodayActivity().first()
        assertEquals(0, savedActivity.waterGlasses)
        assertEquals(0, savedActivity.sleepHours)
        assertEquals(0, savedActivity.exerciseCalories)
        assertEquals(0, savedActivity.steps)
    }


    @Test
    fun updateActivity_withFutureDateAndNoExistingActivity_createsNewActivity() = runTest {
        // Given: Clear existing activities
        fakeActivityDao.clearActivities()

        // When: Update with future date
        val futureTimestamp = testTimestamp + 7 * 24 * 60 * 60 * 1000 // One week in future
        repository.updateActivity(
            listOf(ActivityItemUiState(ActivityType.WATER, 4, 8)),
            futureTimestamp
        )

        // Then: Should create new activity for future date
        assertTrue(fakeActivityDao.activityExistsForDate(futureTimestamp))
    }


    @Test
    fun updateActivity_whenDataChangesBeforeUpdate_usesLatestData() = runTest {
        // Given: Initial setup with activity
        fakeActivityDao.clearActivities()
        fakeActivityDao.addActivity(testActivity)

        // When: Data changes between reading and updating
        val testOperation = async {
            // Start update operation that will read current state
            repository.updateActivity(
                listOf(ActivityItemUiState(ActivityType.WATER, 9, 8)),
                testTimestamp
            )
        }

        // Simulate another process changing the data during the update
        fakeActivityDao.updateActivityByDate(7, 6, 200, 6000, testTimestamp)

        // Complete the update
        testOperation.await()

        // Then: Final state should reflect both changes (water from update operation, others from concurrent change)
        val updatedActivity = fakeActivityDao.getTodayActivity().first()
        assertEquals(9, updatedActivity.waterGlasses)  // From our update
        assertEquals(6, updatedActivity.sleepHours)    // From concurrent change
        assertEquals(200, updatedActivity.exerciseCalories) // From concurrent change
        assertEquals(6000, updatedActivity.steps)      // From concurrent change
    }

    

    @Test
    fun getTodayActivity_whenGoalPreferencesAreMissing_usesZeroGoals() = runTest {
        // Given: Goal preferences are missing or corrupt
        fakeUserProfileDataSource.setGoalPreferences(GoalPreferences(0, 0, 0, 0))

        // When: Getting today's activity
        val result = repository.getTodayActivity().first()

        // Then: Should use zero for all goal readings
        assertNotNull(result)
        result?.forEach { activityItem ->
            assertEquals(0, activityItem.goalReading)
        }
    }
}