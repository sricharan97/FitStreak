package com.apptimistiq.android.fitstreak.Main

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), PermissionRationaleDialog.PermissionDialogListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var permissionDialog: PermissionRationaleDialog


    //ActivityResult launcher to handle the permission request flow
    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->

            if (isGranted) {

                // TODO:Permission is granted. Continue the action or workflow in your app

            } else {
                // TODO:Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied
                Snackbar.make(
                    activityMainBinding.root, getString(R.string.activity_permission_denied),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.ok) {
                        //TODO: Degrade the app feature
                    }
                    .setAnchorView(activityMainBinding.bottomNavView)
                    .show()

            }

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(activityMainBinding.root)


        val host =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment

        val navController = host.navController

        appBarConfiguration =
            AppBarConfiguration(setOf(R.id.home_dest, R.id.recipe_dest, R.id.dashboard_dest))

        val toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)

        toolbar.setupWithNavController(navController, appBarConfiguration)

        val bottomNav = activityMainBinding.bottomNavView

        bottomNav.setupWithNavController(navController)


        //Check if the permissions have been granted for activity tracking in the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkActivityPermission()
        }

        //TODO: When the MainActivity launches, check if the user is a registered user and navigate him
        //accordingly to the login and Onboarding screens.

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkActivityPermission() {
        when {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )

                    == PackageManager.PERMISSION_GRANTED -> {

                //TODO:Permissions are granted.Continue with normal app flow

            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {

                permissionDialog = PermissionRationaleDialog()
                permissionDialog.show(supportFragmentManager, PermissionRationaleDialog.TAG)
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)

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
        return activity?.let {
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
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as PermissionDialogListener

        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }

    }
}
