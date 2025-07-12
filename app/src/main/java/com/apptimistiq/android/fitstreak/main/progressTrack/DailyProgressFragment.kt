package com.apptimistiq.android.fitstreak.main.progressTrack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentDailyProgressBinding
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.*
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.data.DataSource.TYPE_RAW
import com.google.android.gms.fitness.data.DataType.*
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Constants for permission request and logging
 */
private const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 101
private const val LOG_TAG = "DailyProgressFragment"

/**
 * Enum class to differentiate between system-initiated and user-initiated data updates
 */
enum class UpdateType { 
    /** Update initiated by the system during normal data reading */
    SYSTEM_REQUEST, 
    
    /** Update initiated by user interaction */
    USER_REQUEST 
}

/**
 * Fragment responsible for displaying and managing user's daily fitness progress.
 * 
 * This fragment handles:
 * - Google Fit API authentication and permissions
 * - Reading fitness data (steps, calories, sleep, hydration)
 * - Subscribing to fitness data updates
 * - Writing user-provided fitness data to Google Fit
 * - Displaying daily progress in the UI
 */
class DailyProgressFragment : Fragment() {

    // region Properties

    /**
     * View binding for the fragment
     */
    private lateinit var binding: FragmentDailyProgressBinding

    /**
     * ViewModel factory injected by Dagger
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * Shared ViewModel for progress tracking
     */
    private val viewModel by activityViewModels<ProgressViewModel> { viewModelFactory }

    /**
     * Adapter for the activity list RecyclerView
     */
    private lateinit var recyclerAdapter: ActivityListAdapter

    /**
     * Timestamp for the end of calories calculation period
     */
    private var caloriesEndTime: Long = DateTime.now().millis

    /**
     * Google account for authentication with Fitness API
     */
    private lateinit var account: GoogleSignInAccount

    /**
     * Google Fit Recording API client
     */
    private lateinit var recordingClient: RecordingClient

    /**
     * Google Fit History API client
     */
    private lateinit var historyClient: HistoryClient

    /**
     * Google Fit Sessions API client
     */
    private lateinit var sessionsClient: SessionsClient

    /**
     * Google Fit API permissions configuration
     */
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
        .addDataType(TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_HYDRATION, FitnessOptions.ACCESS_WRITE)
        .addDataType(TYPE_HYDRATION, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
        .addDataType(TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
        .addDataType(TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
        .accessSleepSessions(FitnessOptions.ACCESS_WRITE)
        .build()

    /**
     * List of data types needed for fitness tracking
     */
    private val dataTypeList = listOf(
        TYPE_STEP_COUNT_DELTA,
        TYPE_CALORIES_EXPENDED, 
        TYPE_HYDRATION, 
        TYPE_SLEEP_SEGMENT
    )

    // endregion

    // region Fragment Lifecycle Methods

    /**
     * Injects dependencies when the fragment attaches to the context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.dailyProgressComponent().create()
            .inject(this)
    }

    /**
     * Inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, 
            R.layout.fragment_daily_progress, 
            container, 
            false
        )
        return binding.root
    }

    /**
     * Sets up the view and initializes observers after the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up data binding
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Initialize Google Fit clients
        initializeGoogleFitClients()
        
        // Set up RecyclerView adapter
        initializeRecyclerAdapter()
        
        // Check OAuth permissions for Google Fit
        checkForOAuthPermissions()
        
        // Set up UI state observers
        setupUiStateObservers()
        
        // Set up navigation observers
        setupNavigationObservers()
        
        // Set up Fit data update observers
        setupFitDataUpdateObservers()
    }

    /**
     * Handles the result of permission requests
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSION_REQUEST_CODE -> {viewModel.accessGoogleFit()
                Log.d(LOG_TAG, "Google Fit permission granted")}
            else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                Snackbar.make(
                    binding.progressCoordinatorLayoutRoot, 
                    "This app needs permissions to show the activities", 
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) { checkForOAuthPermissions() }
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when the fragment resumes
        // This handles the case where the user grants permission and returns to the app
        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            viewModel.accessGoogleFit()
        }
    }

    // endregion

    // region Initialization Methods
    
    /**
     * Initializes the Google Fit API clients
     */
    private fun initializeGoogleFitClients() {
        //get instance of google account object to use with the API
        account = GoogleSignIn.getAccountForExtension(requireActivity(), fitnessOptions)

        //get Recording API client
        recordingClient = Fitness.getRecordingClient(requireActivity(), account)

        //get History API client
        historyClient = Fitness.getHistoryClient(requireActivity(), account)

        //get Sessions client
        sessionsClient = Fitness.getSessionsClient(requireActivity(), account)
    }
    
    /**
     * Initializes the RecyclerView adapter
     */
    private fun initializeRecyclerAdapter() {
        recyclerAdapter = ActivityListAdapter(ActivityItemListener {
            viewModel.navigateToEditActivity(it)
        })
        binding.recyclerView.adapter = recyclerAdapter
    }
    
    /**
     * Sets up UI state observers to respond to changes in the ViewModel's state
     */
    private fun setupUiStateObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    uiState.userMessages?.let { message ->
                        Snackbar.make(binding.progressCoordinatorLayoutRoot, message, Snackbar.LENGTH_LONG).show()
                        viewModel.userMessageShown()
                    }

                    if (uiState.canAccessGoogleFit && !uiState.subscriptionDone) {
                        Log.d(LOG_TAG, "about to call addsubscriptions inside observer")
                        addSubscriptions()
                    } else if (uiState.canAccessGoogleFit && uiState.subscriptionDone && 
                               !uiState.readSteps && !uiState.readCalories && 
                               !uiState.readSleepHrs && !uiState.readWaterLitres) {
                        Log.d(LOG_TAG, "about to call reading steps inside observer")
                        readDailyStepsTotal()
                        Log.d(LOG_TAG, "about to call reading water inside observer")
                        readDailyHydrationTotal(UpdateType.SYSTEM_REQUEST)
                        Log.d(LOG_TAG, "about to call reading sleep hours inside observer")
                        readDailySleepHrsTotal(UpdateType.SYSTEM_REQUEST)
                        Log.d(LOG_TAG, "about to call reading calories inside observer")
                        readDailyCaloriesExpended(UpdateType.SYSTEM_REQUEST)
                    } else if (uiState.readSteps && uiState.readWaterLitres && uiState.readCalories && 
                              !uiState.activitySavedForDay) {
                        Log.d(LOG_TAG, "about to call saving activity step inside observer")
                        viewModel.saveActivity()
                    }
                }
            }
        }
    }
    
    /**
     * Sets up navigation observers to handle UI navigation events
     */
    private fun setupNavigationObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateEditActivity.collect {
                    when (it) {
                        ActivityType.DEFAULT -> {}
                        ActivityType.STEP -> {
                            Snackbar.make(
                                binding.progressCoordinatorLayoutRoot,
                                "Your steps are auto detected",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            val bundle = bundleOf("act_type" to it)
                            findNavController().navigate(
                                R.id.action_home_dest_to_editActivityFragment,
                                bundle
                            )
                        }
                    }
                    viewModel.navigateToEditActivityCompleted()
                }
            }
        }
    }
    
    /**
     * Sets up observers for Fit data update events from the ViewModel
     */
    private fun setupFitDataUpdateObservers() {
        // Water update observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateFitWater.collect {
                    if (it != 0) {
                        addHydrationData(it)
                        viewModel.fitWaterUpdated()
                    }
                }
            }
        }

        // Sleep update observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateFitSleep.collect {
                    if (it != 0) {
                        addSleepHrsForDay(it)
                        viewModel.fitSleepUpdated()
                    }
                }
            }
        }

        // Exercise update observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateFitExercise.collect {
                    if (it != 0) {
                        addCaloriesData(
                            it, 
                            viewModel.updateFitExerciseStartTimeObs.value,
                            viewModel.updateFitExerciseEndTimeObs.value
                        )
                        viewModel.fitExerciseUpdated()
                    }
                }
            }
        }
    }

    // endregion

    // region Google Fit Authentication

    /**
     * Checks if OAuth permissions have been granted and requests them if needed
     */
    private fun checkForOAuthPermissions() {
        // Check if the user has previously granted the necessary data access
        // If not, initiate the authorization flow
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSION_REQUEST_CODE,
                account,
                fitnessOptions
            )
            Log.d(LOG_TAG, "Requesting Google Fit permissions")
        } else {
            viewModel.accessGoogleFit()
            Log.d(LOG_TAG, "Google Fit permissions already granted")
        }
    }

    // endregion

    // region Google Fit API Operations

    /**
     * Subscribes to required fitness data types using the Recording API
     */
    private fun addSubscriptions() {
        // Get the list of current active subscriptions for the app
        recordingClient.listSubscriptions()
            .addOnSuccessListener { subscriptions ->
                val scDataTypeList = subscriptions.map { it.dataType }

                for (type in dataTypeList) {
                    Log.d(LOG_TAG, "$type is in the subscription list")

                    // If there is no active subscription for the required data types, subscribe
                    if (type !in scDataTypeList) {
                        recordingClient.subscribe(type)
                            .addOnSuccessListener {
                                Log.d(LOG_TAG, "successfully subscribed to data type: ${type.name}")
                            }
                            .addOnFailureListener {
                                Log.d(LOG_TAG, "Failed to subscribe to type: ${type.name}")
                            }
                    } else {
                        Log.d(LOG_TAG, "Already subscribed for data type: ${type.name}")
                    }
                }
                viewModel.doneWithSubscription()
            }
    }

    /**
     * Reads the daily step count total from Google Fit
     */
    private fun readDailyStepsTotal() {
        historyClient.readDailyTotal(dataTypeList[0])
            .addOnSuccessListener { result ->
                val totalSteps =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                Log.d(LOG_TAG, "Total steps until now are: $totalSteps")
                viewModel.addSteps(totalSteps)
            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, 
                    "Exception raised while reading daily steps total: $e"
                )
            }
    }

    /**
     * Reads the daily calories expended from Google Fit
     * 
     * @param updateType Whether this is a system-initiated or user-initiated request
     */
    private fun readDailyCaloriesExpended(updateType: UpdateType) {
        historyClient.readDailyTotal(dataTypeList[1])
            .addOnSuccessListener { result ->
                val totalCalories =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_CALORIES)?.asFloat()
                        ?.toInt() ?: 0
                caloriesEndTime = result.dataPoints.firstOrNull()?.getEndTime(TimeUnit.MILLISECONDS)
                    ?: DateTime.now().millis

                when (updateType) {
                    UpdateType.SYSTEM_REQUEST -> viewModel.addCalories(totalCalories)
                    UpdateType.USER_REQUEST -> viewModel.updateUserEnteredValues(totalCalories)
                }

                Log.d(
                    LOG_TAG,
                    "Total calories expended until now at time: $caloriesEndTime are: $totalCalories"
                )
            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, 
                    "Exception raised while reading daily calories total: $e"
                )
            }
    }

    /**
     * Reads the daily hydration totals from Google Fit
     * 
     * @param updateType Whether this is a system-initiated or user-initiated request
     */
    private fun readDailyHydrationTotal(updateType: UpdateType) {
        historyClient.readDailyTotal(dataTypeList[2])
            .addOnSuccessListener { result ->
                val totalLitres =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_VOLUME)?.asFloat()
                        ?.toInt() ?: 0
                Log.d(LOG_TAG, "Total water consumed until now in litres is: $totalLitres")
                when (updateType) {
                    UpdateType.SYSTEM_REQUEST -> viewModel.addLitres(totalLitres)
                    UpdateType.USER_REQUEST -> viewModel.updateUserEnteredValues(totalLitres)
                }
            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, 
                    "Exception raised while reading daily hydration total: $e"
                )
            }
    }

    /**
     * Adds calories data to Google Fit for a specific time interval
     * 
     * @param caloriesExpended The number of calories expended
     * @param startTime The start time for the activity in HH:mm format
     * @param endTime The end time for the activity in HH:mm format
     */
    private fun addCaloriesData(caloriesExpended: Int, startTime: String, endTime: String) {
        Log.d(LOG_TAG, "Inside addCalories data with value being $caloriesExpended")
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
        val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
        val currentDate = DateTime.now().toString(dateFormat)
        val startDateTime = "$currentDate $startTime"
        val endDateTime = "$currentDate $endTime"

        val startTimeInterval = formatter.parseDateTime(startDateTime).millis
        val endTimeInterval = formatter.parseDateTime(endDateTime).millis
        Log.d(
            LOG_TAG,
            "startTimeString passed: $startTimeInterval and endTimeString passed: $endTimeInterval"
        )

        val caloriesDataSource = DataSource.Builder()
            .setAppPackageName(getString(R.string.app_package))
            .setDataType(TYPE_CALORIES_EXPENDED)
            .setType(TYPE_RAW)
            .build()

        val caloriesDataPoint = DataPoint.builder(caloriesDataSource)
            .setField(Field.FIELD_CALORIES, caloriesExpended.toFloat())
            .setTimeInterval(
                startTimeInterval,
                endTimeInterval,
                TimeUnit.MILLISECONDS
            )
            .build()

        try {
            val caloriesDataset = DataSet.builder(caloriesDataSource)
                .add(caloriesDataPoint)
                .build()

            historyClient.insertData(caloriesDataset)
                .addOnSuccessListener {
                    readDailyCaloriesExpended(UpdateType.USER_REQUEST)
                    Log.d(LOG_TAG, "Successfully inserted calories data of $caloriesExpended")
                }
                .addOnFailureListener {
                    Log.d(
                        LOG_TAG,
                        "Failed to add calories data due to: ${it.message}"
                    )
                }
        } catch (e: IllegalArgumentException) {
            Log.d(LOG_TAG, "Data point out of range: ${e.message}")
        }
    }

    /**
     * Adds hydration data to Google Fit
     * 
     * @param waterLitresConsumed The amount of water consumed in litres
     */
    private fun addHydrationData(waterLitresConsumed: Int) {
        val timestamp = DateTime().minusHours(1).millis
        Log.d(LOG_TAG, "Timestamp passed for hydration data: $timestamp")

        // Create a data source
        val hydrationDataSource = DataSource.Builder()
            .setAppPackageName(getString(R.string.app_package))
            .setDataType(TYPE_HYDRATION)
            .setType(TYPE_RAW)
            .build()

        val hydrationDatapoint = DataPoint.builder(hydrationDataSource)
            .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_VOLUME, waterLitresConsumed.toFloat())
            .build()

        val hydrationDataSet = DataSet.builder(hydrationDataSource)
            .add(hydrationDatapoint)
            .build()

        historyClient.insertData(hydrationDataSet)
            .addOnSuccessListener {
                readDailyHydrationTotal(UpdateType.USER_REQUEST)
                Log.d(LOG_TAG, "Successfully inserted water data of $waterLitresConsumed")
            }
            .addOnFailureListener {
                Log.d(LOG_TAG, "Failed to add hydration data")
            }
    }

    /**
     * Reads the daily sleep hours from Google Fit
     * 
     * @param updateType Whether this is a system-initiated or user-initiated request
     */
    private fun readDailySleepHrsTotal(updateType: UpdateType) {
        val endTime = DateTime.now()
        val startTime = endTime.minusDays(1)

        // Build the session read request first
        val sessionReadRequest = SessionReadRequest.Builder()
            .readSessionsFromAllApps()
            .includeSleepSessions()
            .read(TYPE_SLEEP_SEGMENT)
            .setTimeInterval(startTime.millis, endTime.millis, TimeUnit.MILLISECONDS)
            .build()

        // Read the sleep session using sessions client
        Log.d(LOG_TAG, "Trying to read the sleep hours for the day")
        sessionsClient.readSession(sessionReadRequest)
            .addOnSuccessListener { response ->
                for (session in response.sessions) {
                    val sessionStart = session.getStartTime(TimeUnit.HOURS)
                    val sessionEnd = session.getEndTime(TimeUnit.HOURS)
                    val sleepHrs = (sessionEnd - sessionStart).toInt()
                    Log.d(LOG_TAG, "Total sleep hours are: $sleepHrs")
                    when (updateType) {
                        UpdateType.SYSTEM_REQUEST -> viewModel.addSleepHrs(sleepHrs)
                        UpdateType.USER_REQUEST -> viewModel.updateUserEnteredValues(sleepHrs)
                    }
                }
            }.addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, 
                    "Exception raised while reading daily sleep hours total: $e"
                )
            }
    }

    /**
     * Adds sleep hours data to Google Fit
     * 
     * @param sleepHrs The number of hours slept
     */
    private fun addSleepHrsForDay(sleepHrs: Int) {
        // Give random sleep start and end dates as the exact time is not relevant in
        // our case but for number of hours
        val sleepStart = DateTime.now().minusHours(sleepHrs).millis
        val sleepEnd = DateTime.now().millis

        // Create the sleep session
        val session = Session.Builder()
            .setName("sleepSession")
            .setIdentifier("sleepIdentifier")
            .setDescription("sleep session for the night")
            .setStartTime(sleepStart, TimeUnit.MILLISECONDS)
            .setEndTime(sleepEnd, TimeUnit.MILLISECONDS)
            .setActivity(FitnessActivities.SLEEP)
            .build()

        // Build the request to insert the session.
        val request = SessionInsertRequest.Builder()
            .setSession(session)
            .build()

        sessionsClient.insertSession(request)
            .addOnSuccessListener {
                readDailySleepHrsTotal(UpdateType.USER_REQUEST)
                Log.d(LOG_TAG, "Inserted sleep hrs: ${(sleepEnd - sleepStart) / 3600000}")
            }
            .addOnFailureListener { e ->
                Log.w(LOG_TAG, "Problem inserting the session", e)
            }
    }

    // endregion


}
