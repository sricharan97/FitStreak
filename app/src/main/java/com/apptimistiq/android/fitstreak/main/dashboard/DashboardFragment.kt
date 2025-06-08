package com.apptimistiq.android.fitstreak.main.dashboard

import android.content.Context
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
import com.apptimistiq.android.fitstreak.authentication.AuthDataResult
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.databinding.FragmentDashboardBinding
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard Fragment that serves as the main screen displaying user fitness information and goals.
 *
 * This fragment is responsible for:
 * - Displaying current fitness metrics
 * - Showing progress towards user goals
 * - Providing navigation to edit goal screens
 * - Allowing user to log out
 *
 * Uses Dagger for dependency injection and the MVVM architecture pattern.
 */

private const val LOG_TAG = "DashboardFragment"

class DashboardFragment : Fragment() {

    /** Data binding instance for fragment_dashboard.xml layout */
    private lateinit var binding: FragmentDashboardBinding

    /** Factory for creating ViewModel instances, injected by Dagger */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /** Shared ViewModel instance scoped to the activity */
    private val viewModel by activityViewModels<DashboardViewModel> { viewModelFactory }

    /** Shared Authentication ViewModel instance scoped to the activity */
    private val authViewModel by activityViewModels<AuthenticationViewModel> { viewModelFactory }

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
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
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
        binding.logoutButton.setOnClickListener {
            authViewModel.signOutAndResetData()
        }

        observeNavigationEvents()
        observeAuthenticationState()
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


    private fun observeAuthenticationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isAuthenticated.collect { result ->
                    if (result is AuthDataResult.Success && !result.data) {
                        // User is logged out, navigate to login
                        navigateToLoginDest()
                    } else if (result is AuthDataResult.Error) {
                        // Handle error, e.g., show a Snackbar
                        Snackbar.make(binding.root, "Authentication error", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /**
     * Saves the user's onboarding state and navigates to the home screen.
     * This completes the onboarding flow.
     */
    private fun navigateToLoginDest() {

        if (findNavController().currentDestination?.id == R.id.dashboardFragment) {
            Log.d(LOG_TAG, "User is logged in and onboarded. Navigating to home transition.")
            findNavController().navigate(DashboardFragmentDirections.actionDashboardDestToLoginFragment())
        }
    }
}
