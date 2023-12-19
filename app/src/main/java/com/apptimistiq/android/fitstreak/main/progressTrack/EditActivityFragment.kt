package com.apptimistiq.android.fitstreak.main.progressTrack

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentAddWorkoutBinding
import com.apptimistiq.android.fitstreak.databinding.FragmentEditActivityBinding
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

private const val LOG_TAG = "EditActivityFragment"


class EditActivityFragment : Fragment() {

    private lateinit var binding: FragmentEditActivityBinding
    private lateinit var bindingWorkout: FragmentAddWorkoutBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<ProgressViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {

        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.EditActivityComponent().create()
            .inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingWorkout =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_workout, container, false)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_activity, container, false)

        //Return the binding layout based on the selected activity type
        return when (arguments?.get("act_type")) {
            ActivityType.EXERCISE -> {
                bindingWorkout.root
            }
            else -> {
                binding.root

            }
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        bindingWorkout.lifecycleOwner = this
        bindingWorkout.viewModel = viewModel

        when (arguments?.get("act_type")) {


            ActivityType.SLEEP -> {
                binding.activityEditType.text = resources.getString(R.string.edit_sleep)
                binding.activityValTag.text = resources.getString(R.string.edit_sleep_tag)
            }

            ActivityType.EXERCISE -> {
                bindingWorkout.addWorkoutEditTitle.text =
                    resources.getString(R.string.edit_exercise)
                bindingWorkout.caloriesText.text = resources.getString(R.string.edit_exercise_tag)
            }

            ActivityType.WATER -> {
                binding.activityEditType.text = resources.getString(R.string.edit_water)
                binding.activityValTag.text = resources.getString(R.string.edit_water_tag)
            }
        }

        //
        bindingWorkout.startTimeValue.setOnClickListener {
            TimePickerFragment(it as EditText).show(childFragmentManager, LOG_TAG)
        }

        bindingWorkout.endTimeValue.setOnClickListener {
            TimePickerFragment(it as EditText).show(childFragmentManager, LOG_TAG)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityValueCurrent.collect {
                    viewModel.updateDisplayedActivityVal(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateBackProgress.collect {
                    if (it) {
                        findNavController().navigate(R.id.action_editActivityFragment_to_home_dest)
                        viewModel.navigateBackToProgressFragmentCompleted()
                    }
                }
            }
        }


    }
}

class TimePickerFragment(private val textValue: EditText) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, false)
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        //Assign the formatted time string to the start
        textValue.setText(
            LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
        )

    }

}