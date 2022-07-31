package com.apptimistiq.android.fitstreak.main.progressTrack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.apptimistiq.android.fitstreak.databinding.FragmentDailyProgressBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType.*
import com.google.android.gms.fitness.data.Field
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 101
private const val LOG_TAG = "DailyProgressFragment"


class DailyProgressFragment : Fragment() {

    private lateinit var binding: FragmentDailyProgressBinding

    private val viewModel: ProgressViewModel by activityViewModels()

    //create fitnessOptions instance declaring the data types our app need
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    //get instance of google account object to use with the API
    private val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)

    //get Recording API client
    private val recordingClient = Fitness.getRecordingClient(requireActivity(), account)

    //get the History Client
    private val historyClient = Fitness.getHistoryClient(requireActivity(), account)

    //list of Data types required for the app
    private val dataTypeList = listOf(
        TYPE_STEP_COUNT_CUMULATIVE, TYPE_HYDRATION,
        TYPE_CALORIES_EXPENDED
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDailyProgressBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Check if user has granted necessary Oauth permissions to track the activities
        checkForOAuthPermissions()

        //observer the ui state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.canAccessGoogleFit and !uiState.subscriptionDone) {
                        addSubscriptions()

                    } else if (uiState.canAccessGoogleFit and uiState.subscriptionDone) {
                        readDailyStepsTotal()
                    }

                }
            }
        }

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
                viewModel.updateSteps(totalSteps)

            }
            .addOnFailureListener { e ->
                Log.d(
                    LOG_TAG, "Following exception has been raised while reading" +
                            "daily steps total : $e"
                )

            }

    }

}







