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

private const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 101
private const val LOG_TAG = "DailyProgressFragment"

/**Define a Enum class to decide whether to update the user entered activity value or to just
// read from the fit store **/
enum class UpdateType { SYSTEM_REQUEST, USER_REQUEST }

class DailyProgressFragment : Fragment() {

    private lateinit var binding: FragmentDailyProgressBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<ProgressViewModel> { viewModelFactory }

    private lateinit var recyclerAdapter: ActivityListAdapter

    private var caloriesEndTime: Long = DateTime.now().millis

    //create fitnessOptions instance declaring the data types our app need
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

    private lateinit var account: GoogleSignInAccount

    private lateinit var recordingClient: RecordingClient

    private lateinit var historyClient: HistoryClient

    private lateinit var sessionsClient: SessionsClient


    //list of Data types required for the app
    private val dataTypeList = listOf(
        TYPE_STEP_COUNT_DELTA,
        TYPE_CALORIES_EXPENDED, TYPE_HYDRATION, TYPE_SLEEP_SEGMENT
    )


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.dailyProgressComponent().create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_daily_progress, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        //get instance of google account object to use with the API
        account = GoogleSignIn.getAccountForExtension(requireActivity(), fitnessOptions)

        //get Recording API client
        recordingClient = Fitness.getRecordingClient(requireActivity(), account)

        //get History API client
        historyClient = Fitness.getHistoryClient(requireActivity(), account)

        //get Sessions client
        sessionsClient = Fitness.getSessionsClient(requireActivity(), account)


        //initialize the recyclerAdapter by creating the ActivityListAdapter
        recyclerAdapter = ActivityListAdapter(ActivityItemListener {
            viewModel.navigateToEditActivity(it)
        })


        //Check if user has granted necessary Oauth permissions to track the activities
        checkForOAuthPermissions()

        //observe the ui state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.canAccessGoogleFit and !uiState.subscriptionDone) {
                        Log.d(LOG_TAG, "about to call addsubscriptions inside 0bserver")
                        addSubscriptions()

                    } else if (uiState.canAccessGoogleFit and uiState.subscriptionDone and !uiState.readSteps
                        and !uiState.readCalories and !uiState.readSleepHrs and !uiState.readWaterLitres
                    ) {
                        Log.d(LOG_TAG, "about to call reading steps inside observer")
                        readDailyStepsTotal()
                        Log.d(LOG_TAG, "about to call reading water inside observer")
                        readDailyHydrationTotal(UpdateType.SYSTEM_REQUEST)
                        Log.d(LOG_TAG, "about to call reading sleep hours inside observer")
                        readDailySleepHrsTotal(UpdateType.SYSTEM_REQUEST)
                        Log.d(LOG_TAG, "about to call reading calories inside observer")
                        readDailyCaloriesExpended(UpdateType.SYSTEM_REQUEST)

                    } else if (uiState.readSteps and uiState.readWaterLitres and uiState.readCalories
                        and !uiState.activitySavedForDay
                    ) {
                        Log.d(LOG_TAG, "about to call saving activity step inside observer")
                        viewModel.saveActivity()
                    }

                }
            }
        }

        //observe the triggers to edit activity
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateFitExercise.collect {
                    if (it != 0) {
                        addCaloriesData(
                            it, viewModel.updateFitExerciseStartTimeObs.value,
                            viewModel.updateFitExerciseEndTimeObs.value
                        )
                        viewModel.fitExerciseUpdated()
                    }
                }
            }
        }

        //set the adapter on the recyclerView using Data binding
        binding.recyclerView.adapter = recyclerAdapter

    }


    private fun checkForOAuthPermissions() {

        //check if the user has previously granted the necessary data access
        //if not initiate the authorization flow
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                requireActivity(),
                GOOGLE_FIT_PERMISSION_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            viewModel.accessGoogleFit()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSION_REQUEST_CODE -> viewModel.accessGoogleFit()
                else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                Snackbar.make(
                    binding.progressCoordinatorLayoutRoot, "This app needs permissions to " +
                            "show the activities", Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) { checkForOAuthPermissions() }
                    .show()
            }
        }

    }


    //Subscribe to required data types using Recording API from Google Fit
    private fun addSubscriptions() {

        //Get the list of current active subscriptions for the app
        recordingClient.listSubscriptions()
            .addOnSuccessListener { subscriptions ->

                val scDataTypeList = subscriptions.map { it.dataType }

                for (type in dataTypeList) {
                    Log.d(LOG_TAG, "$type is in the subscription list")

                    //If there is no active subscription for the required data types,subscribe.
                    if (type !in scDataTypeList) {
                        recordingClient.subscribe(type)
                            .addOnSuccessListener {
                                Log.d(
                                    LOG_TAG,
                                    "successfully subscribed to data type : ${type.name}"
                                )
                            }
                            .addOnFailureListener {
                                Log.d(
                                    LOG_TAG,
                                    "There is a failure subscribing to type : ${type.name}"
                                )
                            }
                    } else {
                        Log.d(
                            LOG_TAG,
                            "Already subscribed for data type : ${type.name}"
                        )
                    }

                }
                viewModel.doneWithSubscription()
            }


    }

    private fun readDailyStepsTotal() {


        historyClient.readDailyTotal(dataTypeList[0])
            .addOnSuccessListener { result ->
                val totalSteps =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                Log.d(LOG_TAG, "Total steps until now are : $totalSteps")
                viewModel.addSteps(totalSteps)

            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, "Following exception has been raised while reading" +
                            "daily steps total : $e"
                )

            }

    }


    private fun readDailyCaloriesExpended(updateType: UpdateType) {
        //addHeightAndWeight()


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
                    "Total calories expended until now at time : $caloriesEndTime are : $totalCalories"
                )

            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, "Following exception has been raised while reading" +
                            "daily calories total : $e"
                )
            }

    }

    /*

        private fun addHeightAndWeight() {

            val timestamp = DateTime().millis
            val height = 1.75f
            val weight = 75f

            val heightDataSource = DataSource.Builder()
                .setAppPackageName(getString(R.string.app_package))
                .setDataType(TYPE_HEIGHT)
                .setType(TYPE_RAW)
                .build()

            val weightDataSource = DataSource.Builder()
                .setAppPackageName(getString(R.string.app_package))
                .setDataType(TYPE_WEIGHT)
                .setType(TYPE_RAW)
                .build()

            val heightDataPoint = DataPoint.builder(heightDataSource)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_HEIGHT, height)
                .build()

            val heightDataSet = DataSet.builder(heightDataSource)
                .add(heightDataPoint)
                .build()

            val weightDataPoint = DataPoint.builder(weightDataSource)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_WEIGHT, weight)
                .build()

            val weightDataSet = DataSet.builder(weightDataSource)
                .add(weightDataPoint)
                .build()

            historyClient.insertData(heightDataSet)
                .addOnSuccessListener {
                    Log.d(LOG_TAG, "inserted height of the user - $height")
                }
                .addOnFailureListener { e ->
                    Log.d(LOG_TAG, "there was a problem inserting the height of the user $e")

                }

            historyClient.insertData(weightDataSet)
                .addOnSuccessListener {
                    Log.d(LOG_TAG, "Inserted the weight of the user - $weight")

                }
                .addOnFailureListener { e ->
                    Log.d(LOG_TAG, "there was a problem inserting the weight of the user $e")
                }

        }


    */
    private fun readDailyHydrationTotal(updateType: UpdateType) {

        historyClient.readDailyTotal(dataTypeList[2])
            .addOnSuccessListener { result ->
                val totalLitres =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_VOLUME)?.asFloat()
                        ?.toInt() ?: 0
                Log.d(LOG_TAG, "Total water consumed until now in litres is : $totalLitres")
                when (updateType) {
                    UpdateType.SYSTEM_REQUEST -> viewModel.addLitres(totalLitres)
                    UpdateType.USER_REQUEST -> viewModel.updateUserEnteredValues(totalLitres)
                }

            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, "Following exception has been raised while reading" +
                            "daily hydration total : $e"
                )
            }

    }


    private fun addCaloriesData(caloriesExpended: Int, startTime: String, endTime: String) {
        Log.d(LOG_TAG, "inside addCalories data with value being $caloriesExpended")
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
        val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
        val currentDate = DateTime.now().toString(dateFormat)
        val startDateTime = "$currentDate $startTime"
        val endDateTime = "$currentDate $endTime"

        val startTimeInterval = formatter.parseDateTime(startDateTime).millis
        val endTimeInterval = formatter.parseDateTime(endDateTime).millis
        Log.d(
            LOG_TAG,
            "startTimeString passed :$startTimeInterval and endTimeString passed: $endTimeInterval"
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
                    Log.d(LOG_TAG, "Successfully inserted the calories data of $caloriesExpended")

                }
                .addOnFailureListener {
                    Log.d(
                        LOG_TAG,
                        "Failed to add calories data due to the exception - ${it.message}"
                    )
                }
        } catch (e: IllegalArgumentException) {
            Log.d(LOG_TAG, "data point out of range : ${e.message}")
        }


    }



    private fun addHydrationData(waterLitresConsumed: Int) {

        val timestamp = DateTime().minusHours(1).millis

        Log.d(LOG_TAG, "timestamp passed for hydration data :$timestamp ")

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
                Log.d(LOG_TAG, "Successfully inserted the water data of $waterLitresConsumed")

            }
            .addOnFailureListener {
                Log.d(LOG_TAG, "Failed to add hydration data")
            }

    }


    /**
     * Work with Sessions to read the sleep session data
     */
    private fun readDailySleepHrsTotal(updateType: UpdateType) {

        val endTime = DateTime.now()
        val startTime = endTime.minusDays(1)

        //Build the session read request first
        val sessionReadRequest = SessionReadRequest.Builder()
            .readSessionsFromAllApps()
            .includeSleepSessions()
            .read(TYPE_SLEEP_SEGMENT)
            .setTimeInterval(startTime.millis, endTime.millis, TimeUnit.MILLISECONDS)
            .build()

        //Read the sleep session using sessions client
        Log.d(LOG_TAG, "Trying to read the sleep hours for the day")
        sessionsClient.readSession(sessionReadRequest)
            .addOnSuccessListener { response ->

                for (session in response.sessions) {
                    val sessionStart = session.getStartTime(TimeUnit.HOURS)
                    val sessionEnd = session.getEndTime(TimeUnit.HOURS)
                    val sleepHrs = (sessionEnd - sessionStart).toInt()
                    Log.d(LOG_TAG, "Total sleep hours are : $sleepHrs")
                    when (updateType) {
                        UpdateType.SYSTEM_REQUEST -> viewModel.addSleepHrs(sleepHrs)
                        UpdateType.USER_REQUEST -> viewModel.updateUserEnteredValues(sleepHrs)
                    }
                }

            }.addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, "Following exception has been raised while reading" +
                            "daily sleep hrs total : $e"
                )

            }

    }

    private fun addSleepHrsForDay(sleepHrs: Int) {

        //Give random sleep start and end dates as the exact time is not relevant in
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
                Log.d(LOG_TAG, "inserted the sleep hrs - ${(sleepEnd - sleepStart).div(3600000)}")

            }
            .addOnFailureListener { e ->

                Log.w(LOG_TAG, "There was a problem inserting the session", e)

            }

    }


}







