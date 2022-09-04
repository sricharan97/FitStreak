package com.apptimistiq.android.fitstreak.main.progressTrack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentDailyProgressBinding
import com.apptimistiq.android.fitstreak.main.data.GoalPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType.*
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.SessionReadRequest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 101
private const val LOG_TAG = "DailyProgressFragment"


class DailyProgressFragment : Fragment() {

    private lateinit var binding: FragmentDailyProgressBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<ProgressViewModel> { viewModelFactory }

    private lateinit var recyclerAdapter: ActivityListAdapter

    //GoalPreference values at present
    private lateinit var currentGoalPreferences: GoalPreferences

    //create fitnessOptions instance declaring the data types our app need
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(TYPE_HYDRATION, FitnessOptions.ACCESS_WRITE)
        .addDataType(TYPE_HYDRATION, FitnessOptions.ACCESS_READ)
        .build()

    //get instance of google account object to use with the API
    private val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)

    //get Recording API client
    private val recordingClient = Fitness.getRecordingClient(requireContext(), account)

    //get the History Client
    private val historyClient = Fitness.getHistoryClient(requireContext(), account)

    //get the Sessions Client
    private val sessionsClient = Fitness.getSessionsClient(requireContext(), account)

    //list of Data types required for the app
    private val dataTypeList = listOf(
        TYPE_STEP_COUNT_CUMULATIVE,
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

        //initialize the recyclerAdapter by creating the ActivityListAdapter
        recyclerAdapter = ActivityListAdapter(ActivityItemListener {
            TODO("implement the onclick functionality of activity by handling in the viewmodel")
        })


        //Check if user has granted necessary Oauth permissions to track the activities
        checkForOAuthPermissions()

        //observe the ui state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.canAccessGoogleFit and !uiState.subscriptionDone) {
                        addSubscriptions()

                    } else if (uiState.canAccessGoogleFit and uiState.subscriptionDone) {
                        readDailyStepsTotal()
                        readDailyCaloriesExpended()
                        readDailyHydrationTotal()
                        readDailySleepHrsTotal()
                        viewModel.saveActivity()
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
                TODO("Permission not granted. show a snackbar to the user")
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

    private fun readDailyCaloriesExpended() {
        historyClient.readDailyTotal(dataTypeList[1])
            .addOnSuccessListener { result ->
                val totalCalories =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_CALORIES)?.asFloat()
                        ?.toInt() ?: 0
                Log.d(LOG_TAG, "Total calories expended until now are : $totalCalories")
                viewModel.addCalories(totalCalories)

            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, "Following exception has been raised while reading" +
                            "daily calories total : $e"
                )
            }
    }


    private fun readDailyHydrationTotal() {

        historyClient.readDailyTotal(dataTypeList[2])
            .addOnSuccessListener { result ->
                val totalLitres =
                    result.dataPoints.firstOrNull()?.getValue(Field.FIELD_VOLUME)?.asFloat()
                        ?.toInt() ?: 0
                Log.d(LOG_TAG, "Total water consumed until now in litres is : $totalLitres")
                viewModel.addLitres(totalLitres)
            }
    }


    /**
     * Work with Sessions to read the sleep session data
     */
    private fun readDailySleepHrsTotal() {

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

        sessionsClient.readSession(sessionReadRequest)
            .addOnSuccessListener { response ->

                for (session in response.sessions) {
                    val sessionStart = session.getStartTime(TimeUnit.HOURS)
                    val sessionEnd = session.getEndTime(TimeUnit.HOURS)
                    viewModel.addSleepHrs((sessionEnd - sessionStart).toInt())
                }

            }


    }


}







