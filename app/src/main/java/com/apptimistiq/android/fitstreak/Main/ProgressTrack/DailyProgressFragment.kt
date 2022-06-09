package com.apptimistiq.android.fitstreak.Main.ProgressTrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentDailyProgressBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


class DailyProgressFragment : Fragment() {

    private lateinit var binding: FragmentDailyProgressBinding

    //ActivityResult launcher to handle the permission request flow
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->

            if (isGranted) {

                // TODO:Permission is granted. Continue the action or workflow in your app

            } else {

                // TODO:Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied
                Snackbar.make(
                    binding.root, getString(R.string.permission_denial_text),
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                )

                    .setAction(R.string.snackbar_action)

                    { callPermissionLauncher() }
                    .show()
            }

        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDailyProgressBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Check if the permissions have been granted for activity tracking in the app
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            -> {
                //TODO:Permissions are granted.Continue with normal app flow
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                callPermissionLauncher()

            }
        }
    }

    private fun callPermissionLauncher() {

        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)

    }


}