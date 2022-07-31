package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.authentication.GoalType
import com.apptimistiq.android.fitstreak.databinding.FragmentGoalSelectionBinding


private const val WATER_GLASS_MAX = 100
private const val WATER_GLASS_MIN = 0
private const val SLEEP_HR_MAX = 24
private const val SLEEP_HR_MIN = 0


class GoalSelectionFragment : Fragment() {


    private lateinit var binding: FragmentGoalSelectionBinding

    private val viewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGoalSelectionBinding.inflate(layoutInflater, container, false)
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
                //TODO: read the value from the picker and store it to the viewmodel
                viewModel.saveGoal(GoalType.STEP, newVal)
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
                //TODO: read the value from the picker and store it to the viewmodel
                viewModel.saveGoal(GoalType.WATER, newVal)

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
                //TODO: read the value from the picker and store it to the viewmodel
                viewModel.saveGoal(GoalType.SLEEP, newVal)

            }
        }
    }

    //set up exercise cal picker
    private fun setUpExerciseCalPicker() {

        binding.exerciseCalPicker.apply {
            wrapSelectorWheel = true
            val pickerValuesInt = List(200) { (it * 100) + 100 }
            val pickerValuesStr = pickerValuesInt.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = pickerValuesStr.size - 1
            displayedValues = pickerValuesStr
            setOnValueChangedListener { picker, oldVal, newVal ->
                //TODO: read the value from the picker and store it to the viewmodel
                viewModel.saveGoal(GoalType.EXERCISE, newVal)

            }
        }
    }


    //Navigate to Home screen
    private fun navigateToHomeDest() {

        findNavController().navigate(GoalSelectionFragmentDirections.actionGoalSelectionFragmentToMainActivity())

    }
}