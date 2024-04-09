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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.authentication.GoalType
import com.apptimistiq.android.fitstreak.databinding.FragmentGoalSelectionBinding
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import javax.inject.Inject


private const val WATER_GLASS_MAX = 100
private const val WATER_GLASS_MIN = 0
private const val SLEEP_HR_MAX = 24
private const val SLEEP_HR_MIN = 0

private const val LOG_TAG = "GoalSelectionFragment"

class GoalSelectionFragment : Fragment() {


    private lateinit var binding: FragmentGoalSelectionBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<AuthenticationViewModel> { viewModelFactory }

    private var stepCountGoal = 1000
    private var waterGlassesGoal = 0
    private var sleepHrsGoal = 0
    private var exerciseCalGoal = 100

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.goalSelectionComponent().create()
            .inject(
                this
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_goal_selection, container, false)
        // Inflate the layout for this fragment
        setUpStepCountPicker()
        setUpWaterGlassPicker()
        setUpSleepHourPicker()
        setUpExerciseCalPicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: check if the goals have been selected and then navigate to home


        binding.goalSelectionDoneButton.setOnClickListener {
            viewModel.saveGoal(GoalType.STEP, stepCountGoal)
            viewModel.saveGoal(GoalType.WATER, waterGlassesGoal)
            viewModel.saveGoal(GoalType.SLEEP, sleepHrsGoal)
            viewModel.saveGoal(GoalType.EXERCISE, exerciseCalGoal)
            navigateToHomeDest()
            requireActivity().finish()
        }

    }

    //set up step count picker
    private fun setUpStepCountPicker() {

        binding.stepCountPicker.apply {

            wrapSelectorWheel = true
            val pickerValuesInt = List(491) { (it * 100) + 1000 }
            val pickerValuesStr = pickerValuesInt.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = pickerValuesStr.size - 1
            displayedValues = pickerValuesStr

            setOnValueChangedListener { picker, oldVal, newVal ->
                Log.d(
                    LOG_TAG,
                    "step count goal has been selected with step count - ${((newVal * 100) + 1000)}"
                )
                stepCountGoal = ((newVal * 100) + 1000)

            }
        }
    }

    //set up water glass picker
    private fun setUpWaterGlassPicker() {

        binding.waterGlassPicker.apply {
            minValue = WATER_GLASS_MIN
            maxValue = WATER_GLASS_MAX
            wrapSelectorWheel = true
            setOnValueChangedListener { picker, oldVal, newVal ->
                Log.d(LOG_TAG, "water goal has been selected with litres count - $newVal")
                waterGlassesGoal = newVal

            }
        }

    }

    //set up sleep hour picker
    private fun setUpSleepHourPicker() {

        binding.sleepHourPicker.apply {
            minValue = SLEEP_HR_MIN
            maxValue = SLEEP_HR_MAX
            wrapSelectorWheel = true
            setOnValueChangedListener { picker, oldVal, newVal ->
                Log.d(LOG_TAG, "Sleep goal has been selected with hours count - $newVal")
                sleepHrsGoal = newVal

            }
        }
    }

    //set up exercise cal picker
    private fun setUpExerciseCalPicker() {

        binding.exerciseCalPicker.apply {
            wrapSelectorWheel = true
            val pickerValuesInt = List(60) { (it * 50) + 50 }
            val pickerValuesStr = pickerValuesInt.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = pickerValuesStr.size - 1
            displayedValues = pickerValuesStr
            setOnValueChangedListener { picker, oldVal, newVal ->
                Log.d(
                    LOG_TAG,
                    "Exercise goal has been selected with calories count - ${((newVal * 50) + 50)}"
                )
                exerciseCalGoal = ((newVal * 50) + 50)

            }
        }
    }


    //Navigate to Home screen
    private fun navigateToHomeDest() {

        viewModel.saveUserStateInfo(UserStateInfo(isOnboarded = true))
        findNavController().navigate(GoalSelectionFragmentDirections.actionGoalSelectionFragmentToMainActivity())

    }
}