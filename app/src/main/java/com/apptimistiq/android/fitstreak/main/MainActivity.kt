package com.apptimistiq.android.fitstreak.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity serving as the entry point for the FitStreak application.
 * 
 * This activity sets up the navigation components, bottom navigation, and handles
 * permission-based functionality changes. It observes permission states from the
 * ViewModel and conditionally degrades or upgrades app features based on activity
 * permission status.
 *
 * @author FitStreak Team
 * @version 1.0
 */
class MainActivity : AppCompatActivity() {

    // Navigation & UI Components
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController

    // Dependency Injection
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // ViewModel instance using the injected factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    /**
     * Initializes the activity, sets up navigation, and observes permission-related events.
     * 
     * @param savedInstanceState The saved instance state bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Perform dependency injection
        (application as FitApp).appComponent.mainActivityComponent().create().inject(this)

        // Set up data binding
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupNavigation()
        observePermissionEvents()
    }

    /**
     * Sets up the Navigation component with bottom navigation and toolbar.
     */
    private fun setupNavigation() {
        // Get the NavController from the NavHostFragment
        val host = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = host.navController

        // Configure top-level destinations for the app bar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.daily_progress_fragment,
                R.id.recipe_dest,
                R.id.dashboard_dest
            )
        )

        // Set up the toolbar with navigation
        val toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // Set up bottom navigation
        bottomNav = activityMainBinding.bottomNavView
        bottomNav.setupWithNavController(navController)
    }

    /**
     * Sets up observers for permission-related events from the ViewModel.
     * This includes handling permission denied scenarios and functionality degradation/upgrades.
     */
    private fun observePermissionEvents() {
        // Observer for permission denied events
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityPermissionDenied.collect { permissionDenied ->
                    if (permissionDenied) {
                        showPermissionDeniedSnackbar()
                    }
                    viewModel.resetActivityPermissionDenied()
                }
            }
        }

        // Observer for functionality degradation events
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.degradeHomeFunctionality.collect { degradeHomeFunctionality ->
                    if (degradeHomeFunctionality) {
                        degradeHomeDestinationMenu()
                    }
                    viewModel.resetHomeDestinationMap()
                }
            }
        }

        // Observer for functionality upgrade events
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.upgradeHomeFunctionality.collect { upgradeHomeFunctionality ->
                    if (upgradeHomeFunctionality) {
                        upgradeHomeDestinationMenu()
                    }
                    viewModel.resetUpgradedHomeDestinationMap()
                }
            }
        }
    }

    /**
     * Displays a snackbar informing the user about the denied activity permission
     * and provides an action to degrade functionality gracefully.
     */
    private fun showPermissionDeniedSnackbar() {
        Snackbar.make(
            activityMainBinding.root,
            getString(R.string.activity_permission_denied),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok) {
                viewModel.degradeHomeDestinationMenu()
            }
            .setAnchorView(bottomNav)
            .show()
    }

    /**
     * Reconfigures the bottom navigation to use the degraded home destination
     * when activity permissions are denied.
     * 
     * This redirects users to an alternative experience that doesn't require
     * the denied permission.
     */
    private fun degradeHomeDestinationMenu() {
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.daily_progress_fragment -> {
                    // Navigate to alternative home screen that doesn't require permissions
                    navController.navigate(
                        R.id.homeTransitionFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.homeTransitionFragment) { inclusive = false }
                        }
                    )
                    true
                }
                R.id.recipe_dest -> {
                    navController.navigate(
                        R.id.recipe_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.recipe_dest) { inclusive = false }
                        }
                    )
                    true
                }
                R.id.dashboard_dest -> {
                    navController.navigate(
                        R.id.dashboard_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.dashboard_dest) { inclusive = false }
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Reconfigures the bottom navigation to use the full-featured home destination
     * when activity permissions are granted.
     * 
     * This restores the complete functionality for users who have granted
     * the required permission.
     */
    private fun upgradeHomeDestinationMenu() {
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.daily_progress_fragment -> {
                    // Navigate to full-featured home screen
                    navController.navigate(
                        R.id.daily_progress_fragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.daily_progress_fragment) { inclusive = false }
                        }
                    )
                    true
                }
                R.id.recipe_dest -> {
                    navController.navigate(
                        R.id.recipe_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.recipe_dest) { inclusive = false }
                        }
                    )
                    true
                }
                R.id.dashboard_dest -> {
                    navController.navigate(
                        R.id.dashboard_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.dashboard_dest) { inclusive = false }
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }
}
