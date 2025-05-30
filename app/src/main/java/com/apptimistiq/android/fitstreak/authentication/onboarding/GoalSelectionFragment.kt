package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.databinding.FragmentGoalSelectionBinding
import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Constants for goal selection limits
 */
private const val WATER_GLASS_MIN = 0
private const val WATER_GLASS_MAX = 100
private const val SLEEP_HR_MIN = 0
private const val SLEEP_HR_MAX = 24
private const val LOG_TAG = "GoalSelectionFragment"

/**
 * Fragment for the user goal selection screen during onboarding.
 * 
 * This fragment allows users to set their fitness goals for steps, water intake,
 * sleep hours, and exercise calories. These goals are saved in the ViewModel
 * and will be used throughout the app to track user progress.
 */
class GoalSelectionFragment : Fragment() {

    // Binding instance for accessing views
    private lateinit var binding: FragmentGoalSelectionBinding
    
    // Dagger injection for ViewModelFactory
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    // Shared ViewModel instance
    private val viewModel by activityViewModels<AuthenticationViewModel> { viewModelFactory }
    
    // Goal values to be saved
    private var stepCountGoal = 1000
    private var waterGlassesGoal = 0
    private var sleepHrsGoal = 0
    private var exerciseCalGoal = 100

    /**
     * Lifecycle method called when fragment is attached to an activity.
     * Performs Dagger dependency injection.
     * 
     * @param context The context to which the fragment is attached
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.authenticationComponent().create()
            .inject(this)
    }

    /**
     * Lifecycle method called to create and return the view hierarchy for the fragment.
     * Initializes all goal picker widgets.
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment UI should be attached to
     * @param savedInstanceState Previously saved state to restore from, if available
     * @return The root View of the fragment's layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, 
            R.layout.fragment_goal_selection, 
            container, 
            false
        )
        
        // Set up number picker widgets
        setUpStepCountPicker()
        setUpWaterGlassPicker()
        setUpSleepHourPicker()
        setUpExerciseCalPicker()
        
        return binding.root
    }

    /**
     * Lifecycle method called after the view has been created.
     * Sets up click listeners and handles navigation.
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState Previously saved state, if available
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Handle "Done" button click to save goals and navigate forward
        binding.goalSelectionDoneButton.setOnClickListener {
            // Save all goal values
            saveGoalsAndMarkOnboardingComplete()
        }

        observeUserStateForNavigation()
    }

    private fun saveGoalsAndMarkOnboardingComplete() {
        // Save individual goals
        // Ensure GoalType enum values (STEPS, WATER, SLEEP, EXERCISE) are correct
        viewModel.saveGoal(GoalType.STEP, binding.stepCountPicker.value)
        viewModel.saveGoal(GoalType.WATER, binding.waterGlassPicker.value)
        viewModel.saveGoal(GoalType.SLEEP, binding.sleepHourPicker.value)
        viewModel.saveGoal(GoalType.EXERCISE, binding.exerciseCalPicker.value)

        // Update UserStateInfo to mark onboarding as complete
        viewLifecycleOwner.lifecycleScope.launch {
            val currentState = viewModel.userState.first() // Get the latest state
            // isUserLoggedIn should already be true from the login flow.
            // isOnboarded is the flag we are setting to true here.
            val updatedUserState = currentState.copy(isOnboarded = true)
            viewModel.saveUserStateInfo(updatedUserState)
            Log.d(LOG_TAG, "User state updated to onboarded: true. Waiting for observer to navigate.")
            // The observer will pick up this change and navigate.
        }
    }

    private fun observeUserStateForNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect { userState ->
                    // Check if user is logged in and onboarding is complete
                    if (userState.isUserLoggedIn && userState.isOnboarded) {
                        // Ensure navigation happens only once and from this fragment
                        navigateToHomeDest()
                    }
                }
            }
        }
    }


    // ===== PICKER SETUP METHODS =====

    /**
     * Configures the step count number picker with values from 1000 to 50000,
     * increasing in increments of 100 steps.
     */
    private fun setUpStepCountPicker() {
        binding.stepCountPicker.apply {
            wrapSelectorWheel = true
            val pickerValuesInt = List(491) { (it * 100) + 1000 }
            val pickerValuesStr = pickerValuesInt.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = pickerValuesStr.size - 1
            displayedValues = pickerValuesStr

            setOnValueChangedListener { _, _, newVal ->
                val selectedValue = (newVal * 100) + 1000
                Log.d(LOG_TAG, "Step count goal selected: $selectedValue")
                stepCountGoal = selectedValue
            }
        }
    }

    /**
     * Configures the water glass number picker with values from 0 to 100 glasses.
     */
    private fun setUpWaterGlassPicker() {
        binding.waterGlassPicker.apply {
            minValue = WATER_GLASS_MIN
            maxValue = WATER_GLASS_MAX
            wrapSelectorWheel = true
            
            setOnValueChangedListener { _, _, newVal ->
                Log.d(LOG_TAG, "Water goal selected: $newVal glasses")
                waterGlassesGoal = newVal
            }
        }
    }

    /**
     * Configures the sleep hours number picker with values from 0 to 24 hours.
     */
    private fun setUpSleepHourPicker() {
        binding.sleepHourPicker.apply {
            minValue = SLEEP_HR_MIN
            maxValue = SLEEP_HR_MAX
            wrapSelectorWheel = true
            
            setOnValueChangedListener { _, _, newVal ->
                Log.d(LOG_TAG, "Sleep goal selected: $newVal hours")
                sleepHrsGoal = newVal
            }
        }
    }

    /**
     * Configures the exercise calories number picker with values from 50 to 3000,
     * increasing in increments of 50 calories.
     */
    private fun setUpExerciseCalPicker() {
        binding.exerciseCalPicker.apply {
            wrapSelectorWheel = true
            val pickerValuesInt = List(60) { (it * 50) + 50 }
            val pickerValuesStr = pickerValuesInt.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = pickerValuesStr.size - 1
            displayedValues = pickerValuesStr
            
            setOnValueChangedListener { _, _, newVal ->
                val selectedValue = (newVal * 50) + 50
                Log.d(LOG_TAG, "Exercise goal selected: $selectedValue calories")
                exerciseCalGoal = selectedValue
            }
        }
    }

    /**
     * Saves the user's onboarding state and navigates to the home screen.
     * This completes the onboarding flow.
     */
    private fun navigateToHomeDest() {

        if (findNavController().currentDestination?.id == R.id.goalSelectionFragment) {
            Log.d(LOG_TAG, "User is logged in and onboarded. Navigating to daily progress.")
            findNavController().navigate(R.id.action_goalSelectionFragment_to_daily_progress_fragment)
        }
    }
}
