package com.apptimistiq.android.fitstreak.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.apptimistiq.android.fitstreak.authentication.AuthDataResult
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.databinding.ActivityMainBinding
import com.apptimistiq.android.fitstreak.main.data.domain.NavSetupInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Perform dependency injection
        (application as FitApp).appComponent.mainActivityComponent().create().inject(this)

        // Keep splash screen visible until auth state is determined from the AuthenticationViewModel
        splashScreen.setKeepOnScreenCondition {
            val authLoading = authViewModel.isAuthenticated.value is AuthDataResult.Loading
            val userLoading = authViewModel.userState.value is AuthDataResult.Loading
            authLoading || userLoading
        }

        // Set up data binding
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Initialize NavController early, as it's needed by setupNavigationGraphAndUI
        val host = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = host.navController

        // Initialize authentication state ASAP
        initializeAuthState()

        // Observe UI state from the ViewModel
        observeUIState()
    }

    private fun initializeAuthState() {
        lifecycleScope.launch {
            // Combine authentication states and capture the result
            authViewModel.isAuthenticated.combine(authViewModel.userState) { authResult, userStateResult ->
                if(authResult is AuthDataResult.Success && userStateResult is AuthDataResult.Success) {
                    NavSetupInfo(
                        isAuthenticated = authResult.data,
                        isOnboarded = userStateResult.data.isOnboarded,
                        isReady = true
                    )
                }
                else {
                    NavSetupInfo(false, false, false) // Default to unauthenticated and uninitialized
                }
            }.filter { it.isReady } // Process only when both results are Success
                .collectLatest { navSetUpInfo ->
                    if (!navigationInitialized) {
                        Log.d("MainActivity", "Auth state received: isAuthenticated=${navSetUpInfo.isAuthenticated}, isOnboarded=${navSetUpInfo.isOnboarded}")
                        setupNavigationGraphAndUI(navSetUpInfo.isAuthenticated, navSetUpInfo.isOnboarded)
                        navigationInitialized = true
                    }
                }
        }
    }

    private fun observeUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    // Update bottom nav visibility based on state
                    bottomNav.visibility = if (uiState.bottomNavVisible) View.VISIBLE else View.GONE

                    // Configure navigation based on degradation state
                    if (uiState.degradeHomeFunctionality) {
                        updateNavigationMenuForDegradedHome()
                        // Reset the flag after processing
                        viewModel.resetHomeDestinationMap()
                    }

                    if (uiState.upgradeHomeFunctionality) {
                        updateNavigationMenuForFullHome()
                        viewModel.resetUpgradedHomeDestinationMap()
                    }

                    // Show permission denied snackbar if needed
                    if (uiState.showPermissionDeniedMessage &&
                        navController.currentDestination?.id != R.id.homeTransitionFragment) {
                        showPermissionDeniedSnackbar()
                        viewModel.resetActivityPermissionDenied()
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
                R.id.recipeFragment,
                R.id.dashboardFragment,
                R.id.loginFragment
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
                    viewModel.setBottomNavVisibility(false)
                    supportActionBar?.hide()
                }
                else -> {
                    viewModel.setBottomNavVisibility(true)
                    supportActionBar?.show()
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
     */
    private fun updateNavigationMenuForDegradedHome() {
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
                R.id.recipeFragment -> {
                    navController.navigate(
                        R.id.recipeFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.recipeFragment) { inclusive = false }
                        }
                    )
                    true
                }
                R.id.dashboardFragment -> {
                    navController.navigate(
                        R.id.dashboardFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.dashboardFragment) { inclusive = false }
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
     */
    private fun updateNavigationMenuForFullHome() {
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
                R.id.recipeFragment -> {
                    navController.navigate(
                        R.id.recipeFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.recipeFragment) { inclusive = false }
                        }
                    )
                    true
                }
                R.id.dashboardFragment -> {
                    navController.navigate(
                        R.id.dashboardFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.dashboardFragment) { inclusive = false }
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }
}