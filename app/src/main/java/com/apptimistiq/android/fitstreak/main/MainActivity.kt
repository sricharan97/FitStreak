package com.apptimistiq.android.fitstreak.main

import android.os.Bundle
import android.util.Log
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
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.combine
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
    private var navigationInitialized = false // Flag to ensure navigation is set up only once

    // Dependency Injection
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // ViewModel instances using the injected factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }
    private val authViewModel by viewModels<AuthenticationViewModel> { viewModelFactory }

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

        // Initialize NavController early, as it's needed by setupNavigationGraphAndUI
        val host = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = host.navController

        observeAuthenticationAndSetupNavigation()
    }

    /**
     * Observes authentication state and sets up navigation graph and UI components accordingly.
     * This ensures navigation is configured only after the auth state is determined.
     */
    private fun observeAuthenticationAndSetupNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Combine isAuthenticated and userState to get a single emission for auth status
                authViewModel.isAuthenticated.combine(authViewModel.userState) { isAuthenticated, userState ->
                    Pair(isAuthenticated, userState.isOnboarded)
                }.collect { (isAuth, hasOnboarded) ->
                    // Setup navigation only once with the initial auth state
                    if (!navigationInitialized) {
                        Log.d("MainActivity", "Auth state received: isAuthenticated=$isAuth, isOnboarded=$hasOnboarded. Setting up navigation.")
                        setupNavigationGraphAndUI(isAuth, hasOnboarded)
                        observePermissionEvents() // Now that NavController and UI are fully set up, observe permission events
                        navigationInitialized = true
                    }
                }
            }
        }
    }

    /**
     * Sets up the Navigation component with bottom navigation and toolbar.
     * This method is called once the initial authentication state is known.
     */
    private fun setupNavigationGraphAndUI(isAuthenticated: Boolean, isOnboarded: Boolean) {
        //Inflate the nav_graph XML and override its startDestination
        //based on current authentication / onboarding state
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(
            when {
                !isAuthenticated -> {
                    Log.d("MainActivity", "Setting start destination to LoginFragment")
                    R.id.loginFragment
                }
                !isOnboarded -> {
                    Log.d("MainActivity", "Setting start destination to WelcomeFragment")
                    R.id.welcomeFragment
                }
                else -> {
                    Log.d("MainActivity", "Setting start destination to HomeTransitionFragment")
                    R.id.homeTransitionFragment
                }
            }
        )

        navController.graph = graph

        // Configure top-level destinations for the app bar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.daily_progress_fragment,
                R.id.recipe_dest,
                R.id.dashboard_dest,
                R.id.loginFragment // loginFragment can be a top-level destination if starting there
            )
        )

        // Set up the toolbar with navigation
        val toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // Set up bottom navigation
        bottomNav = activityMainBinding.bottomNavView
        bottomNav.setupWithNavController(navController)

        // Hide bottom navigation for authentication screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.welcomeFragment,
                R.id.goalSelectionFragment,
                R.id.homeTransitionFragment -> {
                    bottomNav.visibility = android.view.View.GONE
                    supportActionBar?.hide() //Also hide ActionBar for these screens
                }
                else -> {
                    bottomNav.visibility = android.view.View.VISIBLE
                    supportActionBar?.show()
                }
            }
        }
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
                        // Only show Snackbar if not on HomeTransitionFragment,
                        // as it has its own UI for this.
                        if (navController.currentDestination?.id != R.id.homeTransitionFragment) {
                            showPermissionDeniedSnackbar()
                        }
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
