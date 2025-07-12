package com.apptimistiq.android.fitstreak.main.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ActivityDaoTest {

    // Subject under test
    private lateinit var activityDao: ActivityDao
    private lateinit var db: ActivityDatabase

    // Test data
    private val todayTimestamp = System.currentTimeMillis()
    private val yesterdayTimestamp = todayTimestamp - TimeUnit.DAYS.toMillis(1)
    private val sixDaysAgoTimestamp = todayTimestamp - TimeUnit.DAYS.toMillis(6)
    private val eightDaysAgoTimestamp = todayTimestamp - TimeUnit.DAYS.toMillis(8)

    // Adjust timestamp to seconds for test compatibility with the DAO query.
    private val activityToday = Activity(dateOfActivity = todayTimestamp / 1000, waterGlasses = 1, sleepHours = 1, exerciseCalories = 1, steps = 1)
    private val activityYesterday = Activity(dateOfActivity = yesterdayTimestamp / 1000, waterGlasses = 2, sleepHours = 2, exerciseCalories = 2, steps = 2)
    private val activitySixDaysAgo = Activity(dateOfActivity = sixDaysAgoTimestamp / 1000, waterGlasses = 3, sleepHours = 3, exerciseCalories = 3, steps = 3)
    private val activityEightDaysAgo = Activity(dateOfActivity = eightDaysAgoTimestamp / 1000, waterGlasses = 4, sleepHours = 4, exerciseCalories = 4, steps = 4)


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ActivityDatabase::class.java)
            .allowMainThreadQueries() // Allow queries on the main thread for testing.
            .build()
        activityDao = db.getActivityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun saveActivity_and_getTodayActivity() = runTest {
        // When: an activity is saved
        activityDao.saveActivity(activityToday)

        // Then: the activity can be retrieved for today
        val todayActivity = activityDao.getTodayActivity().first()
        assertNotNull(todayActivity)
        assertEquals(activityToday.dateOfActivity, todayActivity!!.dateOfActivity)
        assertEquals(activityToday.steps, todayActivity.steps)
    }

    @Test
    fun getTodayActivity_returnsNull_whenNoActivityForToday() = runTest {
        // Given: an activity for yesterday is saved
        activityDao.saveActivity(activityYesterday)

        // When: we get today's activity
        val todayActivity = activityDao.getTodayActivity().first()

        // Then: the result should be null
        assertNull(todayActivity)
    }

    @Test
    fun saveActivity_replacesOnConflict() = runTest {
        // Given: an activity is saved for today
        activityDao.saveActivity(activityToday)
        val initialActivity = activityDao.getTodayActivity().first()
        assertNotNull(initialActivity)

        // When: a new activity with the same date is saved
        val updatedActivity = initialActivity!!.copy(steps = 9999)
        activityDao.saveActivity(updatedActivity)

        // Then: the retrieved activity should have the updated values
        val todayActivity = activityDao.getTodayActivity().first()
        assertNotNull(todayActivity)
        assertEquals(9999, todayActivity!!.steps)
        assertEquals(1, todayActivity.waterGlasses) // Other values remain
    }

    @Test
    fun getWeekActivities_returnsActivitiesInDateRange() = runTest {
        // Given: activities from various dates are saved
        activityDao.saveActivity(activityToday)
        activityDao.saveActivity(activityYesterday)
        activityDao.saveActivity(activitySixDaysAgo)
        activityDao.saveActivity(activityEightDaysAgo) // This one is outside the 7-day range

        // When: we get the week's activities
        val weekActivities = activityDao.getWeekActivities().first()

        // Then: only activities from the last 7 days are returned, ordered by date
        assertEquals(3, weekActivities.size)
        assertEquals(activitySixDaysAgo.dateOfActivity, weekActivities[0].dateOfActivity)
        assertEquals(activityYesterday.dateOfActivity, weekActivities[1].dateOfActivity)
        assertEquals(activityToday.dateOfActivity, weekActivities[2].dateOfActivity)
    }

    @Test
    fun getAllActivities_returnsAllSavedActivities() = runTest {
        // Given
        activityDao.saveActivity(activityToday)
        activityDao.saveActivity(activityYesterday)

        // When
        val allActivities = activityDao.getAllActivities().first()

        // Then
        assertEquals(2, allActivities.size)
    }

    @Test
    fun updateActivityByDate_updatesExistingRecord() = runTest {
        // Given: an activity is saved
        activityDao.saveActivity(activityToday)

        // When: the activity is updated by its date, passing the timestamp in seconds
        activityDao.updateActivityByDate(10, 10, 100, 1000, todayTimestamp / 1000)

        // Then: the retrieved activity reflects the updates
        val updated = activityDao.getTodayActivity().first()
        assertNotNull(updated)
        assertEquals(10, updated!!.waterGlasses)
        assertEquals(10, updated.sleepHours)
        assertEquals(100, updated.exerciseCalories)
        assertEquals(1000, updated.steps)
    }

    @Test
    fun activityExistsForDate_returnsTrueWhenExists() = runTest {
        // Given
        activityDao.saveActivity(activityToday)

        // When/Then
        assertTrue(activityDao.activityExistsForDate(todayTimestamp / 1000))
    }

    @Test
    fun activityExistsForDate_returnsFalseWhenNotExists() = runTest {
        // When/Then
        assertFalse(activityDao.activityExistsForDate(todayTimestamp / 1000))
    }
}