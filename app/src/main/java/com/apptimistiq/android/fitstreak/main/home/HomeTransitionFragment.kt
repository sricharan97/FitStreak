package com.apptimistiq.android.fitstreak.main.home

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.apptimistiq.android.fitstreak.main.MainViewModel
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

    // --- Permission Handling ---
    
    /**
     * Permission request launcher that handles the result of the permission request.
     * The callback determines what happens after the user makes a decision about the permission.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            viewModel.handlePermissionResult(isGranted)
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

        // Observe functionality degradation state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.degradeHomeFunctionality.collect { isDegraded ->
                    if (isDegraded) {
                        binding.permissionDeniedMessageTextview.visibility = View.VISIBLE
                        binding.retryPermissionButton.visibility = View.VISIBLE
                        binding.homePlaceholderImageView.visibility = View.GONE
                    } else {
                        binding.permissionDeniedMessageTextview.visibility = View.GONE
                        binding.retryPermissionButton.visibility = View.GONE
                        binding.homePlaceholderImageView.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.retryPermissionButton.setOnClickListener {
            // Reset relevant states in ViewModel before retrying if necessary
            // For now, directly call checkActivityPermission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkActivityPermission()
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
        viewModel.handlePermissionResult(false)
        permissionDialog.dismiss()
    }

    /**
     * Handles dialog dismissal without an explicit positive or negative choice by the user.
     * This typically occurs if the user taps outside the dialog or presses the back button.
     * Treats this scenario as a denial of permission for functionality.
     */
    override fun onDialogDismissedWithoutExplicitChoice() {
        viewModel.handlePermissionResult(false)
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
                viewModel.handlePermissionResult(true)
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
    private var explicitChoiceMade: Boolean = false

    /**
     * Interface to communicate dialog events back to the host fragment
     */
    interface PermissionDialogListener {
        /** Called when user clicks the positive/accept button */
        fun onDialogPositiveClick(dialog: DialogFragment)
        
        /** Called when user clicks the negative/reject button */
        fun onDialogNegativeClick(dialog: DialogFragment)

        /** Called when the dialog is dismissed without the user making an explicit choice via buttons */
        fun onDialogDismissedWithoutExplicitChoice()
    }

    /**
     * Creates the dialog UI with permission explanation message and buttons
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        explicitChoiceMade = false // Reset flag each time dialog is created
        return context?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.permission_rationale_dialog)
                .setPositiveButton(R.string.permission_dialog_accept) { _, _ ->
                    explicitChoiceMade = true
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(R.string.permission_dialog_reject) { _, _ ->
                    explicitChoiceMade = true
                    listener.onDialogNegativeClick(this)
                }
                .create()
        } ?: throw IllegalStateException("Context cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!explicitChoiceMade) {
            listener.onDialogDismissedWithoutExplicitChoice()
        }
    }

    /**
     * Sets the listener for dialog button clicks
     */
    fun setListener(listener: PermissionDialogListener) {
        this.listener = listener
    }
}
