package com.apptimistiq.android.fitstreak.main

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), PermissionRationaleDialog.PermissionDialogListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var permissionDialog: PermissionRationaleDialog
    private lateinit var host: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNav: BottomNavigationView

    //ActivityResult launcher to handle the permission request flow
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->

                if (isGranted) {

                    attachAndSetupHostFragment()

                } else {
                    // Explain to the user that the feature is unavailable because the
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

        //Check if the permissions have been granted for activity tracking in the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkActivityPermission()
        }


        //TODO: When the MainActivity launches, check if the user is a registered user and navigate him
        //accordingly to the login and Onboarding screens.

    }

    private fun attachAndSetupHostFragment() {
        //Hide the placeholder image once the permissions are granted.
        activityMainBinding.homePlaceholderImageView.visibility = View.GONE
        host =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment

        navController = host.navController

        navController.setGraph(R.navigation.nav_graph)

        appBarConfiguration =
            AppBarConfiguration(setOf(R.id.home_dest, R.id.recipe_dest, R.id.dashboard_dest))

        toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)

        toolbar.setupWithNavController(navController, appBarConfiguration)

        bottomNav = activityMainBinding.bottomNavView

        bottomNav.setupWithNavController(navController)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkActivityPermission() {
        when {

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )

                    == PackageManager.PERMISSION_GRANTED -> {

                attachAndSetupHostFragment()

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
