package com.apptimistiq.android.fitstreak.main.home

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeTransitionFragment : Fragment(), PermissionRationaleDialog.PermissionDialogListener {

    private lateinit var binding: FragmentHomeTransitionBinding
    private lateinit var permissionDialog: PermissionRationaleDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<MainViewModel> { viewModelFactory }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // When permission is granted, explicitly upgrade the home menu
                viewModel.upgradeHomeDestinationMenu()
                // Then signal readiness to navigate
                viewModel.readyToNavigateToDailyProgress()
            } else {
                viewModel.handlePermissionResult(false)
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.homeTransitionComponent()
            .create().inject(this)
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Activate permission check on screen entry
        viewModel.activatePermissionStatusCheck()

        // Observe UI state to update UI elements and handle navigation
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    // Update UI based on state
                    binding.apply {
                        permissionDeniedMessageTextview.visibility =
                            if (uiState.isHomeScreenDegraded) View.VISIBLE else View.GONE
                        retryPermissionButton.visibility =
                            if (uiState.isHomeScreenDegraded) View.VISIBLE else View.GONE
                        homePlaceholderImageView.visibility =
                            if (uiState.isHomeScreenDegraded) View.GONE else View.VISIBLE
                    }

                    // Handle permission check
                    if (uiState.isPermissionCheckInProgress) {
                        checkActivityPermission()
                        viewModel.activityPermissionCheckComplete()
                    }

                    // Handle navigation based on state
                    if (uiState.navigateToDailyProgress) {
                        navigateToDailyProgressFragment()
                        viewModel.navigationToDailyProgressComplete()
                    }

                    // Handle degradation functionality
                    if (uiState.degradeHomeFunctionality) {
                        // After the home has been degraded, reset the flag
                        viewModel.resetHomeDestinationMap()
                    }
                }
            }
        }

        binding.retryPermissionButton.setOnClickListener {
            checkActivityPermission()
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        permissionDialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        viewModel.handlePermissionResult(false)
        permissionDialog.dismiss()
    }

    override fun onDialogDismissedWithoutExplicitChoice() {
        viewModel.handlePermissionResult(false)
    }

    private fun checkActivityPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // When permission is already granted, explicitly upgrade the home menu
                viewModel.upgradeHomeDestinationMenu()
                viewModel.readyToNavigateToDailyProgress()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
                permissionDialog = PermissionRationaleDialog()
                permissionDialog.setListener(this)
                permissionDialog.show(childFragmentManager, PermissionRationaleDialog.TAG)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    private fun navigateToDailyProgressFragment() {
        findNavController().navigate(R.id.action_homeTransitionFragment_to_daily_progress_fragment)
    }
}

// PermissionRationaleDialog remains unchanged
class PermissionRationaleDialog : DialogFragment() {

    companion object {
        const val TAG = "PermissionRationaleDialog"
    }

    private lateinit var listener: PermissionDialogListener
    private var explicitChoiceMade: Boolean = false

    interface PermissionDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
        fun onDialogDismissedWithoutExplicitChoice()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        explicitChoiceMade = false
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

    fun setListener(listener: PermissionDialogListener) {
        this.listener = listener
    }
}