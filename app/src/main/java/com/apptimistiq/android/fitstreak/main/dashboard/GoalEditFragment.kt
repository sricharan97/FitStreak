/**
 * Copyright (c) 2023 Apptimistiq. All rights reserved.
 *
 * FitStreak - Fitness Tracking Application
 * 
 * This fragment allows users to edit their fitness goals and personal information.
 * It displays different input fields based on the type of goal being edited
 * (steps, exercise, sleep, water, height, weight).
 */
package com.apptimistiq.android.fitstreak.main.dashboard

import android.content.Context
import android.os.Bundle
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
import com.apptimistiq.android.fitstreak.databinding.FragmentGoalEditBinding
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Constant used for logging.
 */
private const val LOG_TAG = "GoalEditFragment"

/**
 * Fragment that allows users to edit fitness goals and personal information.
 * 
 * This fragment receives the type of goal/information to edit from navigation arguments
 * and configures the UI accordingly. It uses a shared [DashboardViewModel] to interact
 * with the data layer and navigate back when edits are complete.
 */
class GoalEditFragment : Fragment() {

    // region Properties
    
    /**
     * View binding for the fragment layout.
     */
    private lateinit var binding: FragmentGoalEditBinding

    /**
     * Factory for ViewModels, injected by Dagger.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * Shared ViewModel for dashboard-related data operations.
     * Scoped to the activity and injected using the viewModelFactory.
     */
    private val viewModel by activityViewModels<DashboardViewModel> { viewModelFactory }
    
    // endregion

    // region Lifecycle Methods

    /**
     * Injects dependencies when fragment is attached to context.
     * 
     * @param context The context to which the fragment is attached
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.GoalEditComponent().create()
            .inject(this)
    }

    /**
     * Inflates the fragment layout using data binding.
     * 
     * @param inflater The layout inflater
     * @param container The parent view
     * @param savedInstanceState Any saved state from previous instances
     * @return The root view of the fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_goal_edit, container, false)
        return binding.root
    }

    /**
     * Configures the UI and sets up data collection once the view is created.
     * 
     * @param view The root view of the fragment
     * @param savedInstanceState Any saved state from previous instances
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDataBinding()
        observeViewModel()
        configureUIBasedOnGoalType()
    }
    
    // endregion

    // region Private Helper Methods
    
    /**
     * Sets up data binding with the ViewModel and lifecycle owner.
     */
    private fun setupDataBinding() {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    /**
     * Observes ViewModel state changes to update UI and handle navigation events.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goalInfoVal.collect { goalInfoVal ->
                    viewModel.updateDisplayedGoalInfoVal(goalInfoVal)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateBackToDashboard.collect {
                    if (it) {
                        findNavController().navigate(R.id.action_goalEditFragment_to_dashboard_dest)
                        viewModel.navigateBackDashboardFragmentComplete()
                    }
                }
            }
        }
    }

    /**
     * Configures UI elements based on the type of goal/information being edited.
     * Sets appropriate text and visibility for UI elements based on the goal type.
     */
    private fun configureUIBasedOnGoalType() {
        when (arguments?.get("info_type")) {
            GoalUserInfo.STEPS -> {
                binding.editType.text = resources.getString(R.string.step_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.step_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.step_goal_tag)
            }

            GoalUserInfo.EXERCISE -> {
                binding.editType.text = resources.getString(R.string.exercise_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.exercise_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.exercise_goal_tag)
            }

            GoalUserInfo.SLEEP -> {
                binding.editType.text = resources.getString(R.string.sleep_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.sleep_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.sleep_goal_tag)
            }

            GoalUserInfo.WATER -> {
                binding.editType.text = resources.getString(R.string.water_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.water_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.water_goal_tag)
            }

            GoalUserInfo.HEIGHT -> {
                binding.editType.text = resources.getString(R.string.height_info)
                binding.editTypeDescp.visibility = View.INVISIBLE
                binding.goalTag.text = resources.getString(R.string.height_info_tag)
            }

            GoalUserInfo.WEIGHT -> {
                binding.editType.text = resources.getString(R.string.weight_info)
                binding.editTypeDescp.visibility = View.INVISIBLE
                binding.goalTag.text = resources.getString(R.string.weight_info_tag)
            }
        }
    }
    
    // endregion
}
