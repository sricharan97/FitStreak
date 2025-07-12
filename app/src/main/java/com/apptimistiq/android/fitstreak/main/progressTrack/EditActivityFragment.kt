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
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

private const val LOG_TAG = "EditActivityFragment"

/**
 * Fragment for editing activity data.
 * 
 * This fragment handles editing different types of activities (Sleep, Exercise, Water),
 * with dynamically selected layouts based on activity type. It collects data from the user
 * and updates the activity values through the shared ViewModel.
 */
class EditActivityFragment : Fragment() {

    private lateinit var binding: FragmentEditActivityBinding
    private lateinit var bindingWorkout: FragmentAddWorkoutBinding

    // Dependency injection for ViewModel factory
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<ProgressViewModel> { viewModelFactory }

    /**
     * Performs dependency injection when fragment attaches to context.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.EditActivityComponent().create()
            .inject(this)
    }

    /**
     * Creates and returns the fragment's UI view.
     * Dynamically inflates different layouts based on the activity type.
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment UI will be attached to
     * @param savedInstanceState Previously saved state of the fragment
     * @return The root View of the fragment's layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate both layouts - we'll choose which to use based on activity type
        bindingWorkout =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_workout, container, false)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_activity, container, false)

        // Return the appropriate layout based on activity type
        return when (arguments?.get("act_type")) {
            ActivityType.EXERCISE -> bindingWorkout.root
            else -> binding.root
        }
    }

    /**
     * Configures the view after it's created.
     * Sets up data binding, text fields, and click listeners based on activity type.
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState Previously saved state of the fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure data binding
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        bindingWorkout.lifecycleOwner = this
        bindingWorkout.viewModel = viewModel

        if (savedInstanceState == null) {
            arguments?.getSerializable("act_type")?.let {
                viewModel.prepareForEditing(it as ActivityType)
            }
        }

        // Set up UI text based on activity type
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

        // Set up time pickers for workout form
        bindingWorkout.startTimeValue.setOnClickListener {
            TimePickerFragment(it as EditText).show(childFragmentManager, LOG_TAG)
        }

        bindingWorkout.endTimeValue.setOnClickListener {
            TimePickerFragment(it as EditText).show(childFragmentManager, LOG_TAG)
        }

        // Set up navigation handling
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

/**
 * DialogFragment for selecting time values.
 *
 * Displays a time picker dialog and updates the provided EditText field
 * with the selected time in HH:mm format.
 *
 * @property textValue The EditText field to update with the selected time
 */
class TimePickerFragment(private val textValue: EditText) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    /**
     * Creates the time picker dialog.
     *
     * @param savedInstanceState Previously saved state of the fragment
     * @return The configured TimePickerDialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, false)
    }

    /**
     * Called when the user sets a time in the time picker dialog.
     * Updates the associated EditText with the formatted time.
     *
     * @param view The TimePicker view
     * @param hourOfDay The hour that was set
     * @param minute The minute that was set
     */
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        // Assign the formatted time string to the EditText
        textValue.setText(
            LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }
}