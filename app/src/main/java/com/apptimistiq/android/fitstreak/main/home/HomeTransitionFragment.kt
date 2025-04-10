package com.apptimistiq.android.fitstreak.main.home

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.apptimistiq.android.fitstreak.databinding.FragmentHomeTransitionBinding
import com.apptimistiq.android.fitstreak.main.MainActivity
import com.apptimistiq.android.fitstreak.main.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeTransitionFragment serves as an intermediate screen handling activity permission checks
 * before navigating to the main daily progress screen.
 *
 * This fragment is responsible for:
 * 1. Checking and requesting necessary activity recognition permissions
 * 2. Showing permission rationale when needed
 * 3. Transitioning to the Daily Progress screen when permission handling is complete
 */
class HomeTransitionFragment : Fragment(), PermissionRationaleDialog.PermissionDialogListener {

    // --- Properties ---
    
    private lateinit var binding: FragmentHomeTransitionBinding
    private lateinit var permissionDialog: PermissionRationaleDialog

    /** ViewModel factory provided by Dagger */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /** Shared ViewModel instance */
    private val viewModel by activityViewModels<MainViewModel> { viewModelFactory }

    /** Access to the BottomNavigationView from the hosting activity */
    private val bottomNavigationView =
        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)

    // --- Permission Handling ---
    
    /**
     * Permission request launcher that handles the result of the permission request.
     * The callback determines what happens after the user makes a decision about the permission.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, proceed with normal app flow
                viewModel.readyToNavigateToDailyProgress()
            } else {
                // Permission denied, update the ViewModel
                viewModel.activityPermissionDenied()
            }
        }

    // --- Lifecycle Methods ---

    /**
     * Performs dependency injection when fragment attaches to context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.homeTransitionComponent()
            .create().inject(this)
    }

    /**
     * Inflates the fragment layout and sets up data binding
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, 
            R.layout.fragment_home_transition, 
            container, 
            false
        )
        return binding.root
    }

    /**
     * Sets up the UI state collection and observers after the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe navigation state changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToDailyProgress.collect { navigate ->
                    if (navigate) {
                        navigateToDailyProgressFragment()
                        viewModel.upgradeHomeDestinationMenu()
                    }
                    viewModel.navigationToDailyProgressComplete()
                }
            }
        }

        // Observe permission check requests
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityPermissionStatusCheck.collect { check ->
                    if (check) {
                        checkActivityPermission()
                    }
                    viewModel.activityPermissionCheckComplete()
                }
            }
        }
    }

    // --- Permission Dialog Callbacks ---
    
    /**
     * Handles positive click on permission rationale dialog
     * Launches the system permission request
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        permissionDialog.dismiss()
    }

    /**
     * Handles negative click on permission rationale dialog
     * Continues with limited app functionality
     */
    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // Continue with limited functionality when permission is denied
        permissionDialog.dismiss()
    }

    // --- Helper Methods ---
    
    /**
     * Checks the current state of activity recognition permission and takes appropriate action:
     * - If already granted, proceed with navigation
     * - If rationale should be shown, display permission dialog
     * - Otherwise, directly request the permission
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkActivityPermission() {
        when {
            // Permission is already granted
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.readyToNavigateToDailyProgress()
            }
            
            // Show rationale if needed before requesting permission
            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
                permissionDialog = PermissionRationaleDialog()
                permissionDialog.setListener(this)
                permissionDialog.show(childFragmentManager, PermissionRationaleDialog.TAG)
            }
            
            // Directly request permission
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    /**
     * Navigates to the Daily Progress fragment
     */
    private fun navigateToDailyProgressFragment() {
        findNavController().navigate(R.id.action_homeTransitionFragment_to_daily_progress_fragment)
    }
}

/**
 * Dialog fragment that explains to users why the activity recognition permission is needed
 * and allows them to accept or reject the permission request.
 */
class PermissionRationaleDialog : DialogFragment() {

    companion object {
        /** Tag used for fragment manager transactions */
        const val TAG = "PermissionRationaleDialog"
    }

    /** Listener for dialog button clicks */
    private lateinit var listener: PermissionDialogListener

    /**
     * Interface to communicate dialog events back to the host fragment
     */
    interface PermissionDialogListener {
        /** Called when user clicks the positive/accept button */
        fun onDialogPositiveClick(dialog: DialogFragment)
        
        /** Called when user clicks the negative/reject button */
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    /**
     * Creates the dialog UI with permission explanation message and buttons
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return context?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.permission_rationale_dialog)
                .setPositiveButton(R.string.permission_dialog_accept) { _, _ ->
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(R.string.permission_dialog_reject) { _, _ ->
                    listener.onDialogNegativeClick(this)
                }
                .create()
        } ?: throw IllegalStateException("Context cannot be null")
    }

    /**
     * Sets the listener for dialog button clicks
     */
    fun setListener(listener: PermissionDialogListener) {
        this.listener = listener
    }
}
