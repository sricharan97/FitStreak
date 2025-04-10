package com.apptimistiq.android.fitstreak.main.dashboard

import android.content.Context
import android.os.Bundle
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
import com.apptimistiq.android.fitstreak.databinding.FragmentDashboardBinding
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard Fragment that serves as the main screen displaying user fitness information and goals.
 *
 * This fragment is responsible for:
 * - Displaying current fitness metrics
 * - Showing progress towards user goals
 * - Providing navigation to edit goal screens
 *
 * Uses Dagger for dependency injection and the MVVM architecture pattern.
 */
class DashboardFragment : Fragment() {

    /** Data binding instance for fragment_dashboard.xml layout */
    private lateinit var binding: FragmentDashboardBinding

    /** Factory for creating ViewModel instances, injected by Dagger */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /** Shared ViewModel instance scoped to the activity */
    private val viewModel by activityViewModels<DashboardViewModel> { viewModelFactory }

    /**
     * Called when fragment is attached to context. Handles dependency injection.
     *
     * @param context The context the fragment is attached to
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.DashboardComponent().create()
            .inject(this)
    }

    /**
     * Inflates the fragment layout and initializes data binding.
     *
     * @return The inflated view with data binding applied
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        return binding.root
    }

    /**
     * Sets up UI components and observes LiveData from ViewModel.
     *
     * @param view The created view
     * @param savedInstanceState Saved instance state bundle
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDataBinding()
        observeNavigationEvents()
    }

    /**
     * Configures data binding with lifecycle owner and viewmodel
     */
    private fun setupDataBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    /**
     * Observes navigation events from the ViewModel to handle navigation to edit goal screen
     */
    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToEditGoal.collect {
                    if (it != GoalUserInfo.DEFAULT) {
                        val bundle = bundleOf("info_type" to it)
                        findNavController().navigate(
                            R.id.action_dashboard_dest_to_goalEditFragment,
                            bundle
                        )
                        viewModel.navigateToEditGoalCompleted()
                    }
                }
            }
        }
    }
}
