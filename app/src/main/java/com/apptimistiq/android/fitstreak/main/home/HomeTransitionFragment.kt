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
import androidx.annotation.VisibleForTesting
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

open class HomeTransitionFragment : Fragment(), PermissionRationaleDialog.PermissionDialogListener {

    private lateinit var binding: FragmentHomeTransitionBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    internal val viewModel by activityViewModels<MainViewModel> { viewModelFactory }

    @VisibleForTesting
    var shouldCheckPermissionsOnStart = true

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onPermissionResult(
                isGranted = isGranted,
                shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)
            )
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



        observeUiState()
        observePermissionEvents()

        if (shouldCheckPermissionsOnStart) {
            checkActivityPermission()
        }

        binding.retryPermissionButton.setOnClickListener {
            checkActivityPermission()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    val isPermissionDenied = !uiState.isActivityPermissionGranted
                    binding.permissionDeniedMessageTextview.visibility =
                        if (isPermissionDenied) View.VISIBLE else View.GONE
                    binding.retryPermissionButton.visibility =
                        if (isPermissionDenied) View.VISIBLE else View.GONE
                    binding.homePlaceholderImageView.visibility =
                        if (isPermissionDenied) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun observePermissionEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.permissionEvent.collect { event ->
                    if (event != null) {
                        when (event) {
                            is PermissionEvent.NavigateToDailyProgress -> {
                                findNavController().navigate(R.id.action_homeTransitionFragment_to_daily_progress_fragment)
                            }
                            is PermissionEvent.ShowPermissionRationale -> {
                                val dialog = PermissionRationaleDialog()
                                dialog.setListener(this@HomeTransitionFragment)
                                dialog.show(childFragmentManager, PermissionRationaleDialog.TAG)
                            }
                        }
                        viewModel.onPermissionEventHandled()
                    }
                }
            }
        }
    }

    internal open fun checkActivityPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionResult(isGranted = true, shouldShowRationale = false)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
                viewModel.onPermissionResult(isGranted = false, shouldShowRationale = true)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        dialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User has seen the rationale and denied it. The UI is already in the
        // degraded state, so we just dismiss the dialog.
        dialog.dismiss()
    }

    override fun onDialogDismissedWithoutExplicitChoice() {
        // Same as negative click. The dialog is already being dismissed.
    }
}

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