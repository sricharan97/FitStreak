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

class HomeTransitionFragment : Fragment(), PermissionRationaleDialog.PermissionDialogListener {

    private lateinit var binding: FragmentHomeTransitionBinding
    private lateinit var permissionDialog: PermissionRationaleDialog

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<MainViewModel> { viewModelFactory }

    private val bottomNavigationView =
        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->

            if (isGranted) {

                //TODO("Continue with the normal app flow")
                viewModel.readyToNavigateToDailyProgress()


            } else {
                viewModel.activityPermissionDenied()

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
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home_transition, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        permissionDialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        //TODO: continue using your app with degraded functionality
        permissionDialog.dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkActivityPermission() {
        when {

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )

                    == PackageManager.PERMISSION_GRANTED -> {

                viewModel.readyToNavigateToDailyProgress()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {

                permissionDialog = PermissionRationaleDialog()
                permissionDialog.setListener(this)
                permissionDialog.show(childFragmentManager, PermissionRationaleDialog.TAG)
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)

            }
        }
    }

    private fun navigateToDailyProgressFragment() {
        findNavController().navigate(R.id.action_homeTransitionFragment_to_daily_progress_fragment)
    }
}


class PermissionRationaleDialog : DialogFragment() {

    companion object {
        const val TAG = "PermissionRationaleDialog"
    }

    // Use this instance of the interface to deliver action events
    private lateinit var listener: PermissionDialogListener

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    interface PermissionDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return context?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.permission_rationale_dialog)
                .setPositiveButton(R.string.permission_dialog_accept)
                { dialog, id ->
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(R.string.permission_dialog_reject) { dialog, id ->
                    listener.onDialogNegativeClick(this)
                }


            builder.create()
        } ?: throw IllegalStateException("Context cannot be null")
    }

    fun setListener(listener: PermissionDialogListener) {
        this.listener = listener
    }

    /*// Override the Fragment.onAttach() method to instantiate the PermissionDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface

        try {
            // Instantiate the PermissionDialogListener so we can send events to the host
            listener = context as PermissionDialogListener

        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement PermissionDialogListener")
            )
        }

    }*/
}