package com.apptimistiq.android.fitstreak.main

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
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
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.authentication.AuthDataResult
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.databinding.ActivityMainBinding
import com.apptimistiq.android.fitstreak.main.data.domain.NavSetupInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController
    private var navigationInitialized = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<MainViewModel> { viewModelFactory }
    private val authViewModel by viewModels<AuthenticationViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        (application as FitApp).appComponent.mainActivityComponent().create().inject(this)

        splashScreen.setKeepOnScreenCondition {
            val authLoading = authViewModel.isAuthenticated.value is AuthDataResult.Loading
            val userLoading = authViewModel.userState.value is AuthDataResult.Loading
            authLoading || userLoading
        }

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bottomNav = activityMainBinding.bottomNavView

        val host = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = host.navController

        initializeAuthState()
        observeUIState()
        setupCustomBackPress()
    }

    private fun setupCustomBackPress() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestinationId = navController.currentDestination?.id
                if (currentDestinationId == R.id.daily_progress_fragment) {
                    finish()
                } else if (currentDestinationId == R.id.dashboardFragment || currentDestinationId == R.id.recipeFragment) {
                    navController.navigate(R.id.daily_progress_fragment)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun initializeAuthState() {
        lifecycleScope.launch {
            authViewModel.isAuthenticated.combine(authViewModel.userState) { authResult, userStateResult ->
                if (authResult is AuthDataResult.Success && userStateResult is AuthDataResult.Success) {
                    NavSetupInfo(
                        isAuthenticated = authResult.data,
                        isOnboarded = userStateResult.data.isOnboarded,
                        isReady = true
                    )
                } else {
                    NavSetupInfo(false, false, false)
                }
            }.filter { it.isReady }
                .collectLatest { navSetUpInfo ->
                    if (!navigationInitialized) {
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
                    bottomNav.visibility = if (uiState.bottomNavVisible) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupNavigationGraphAndUI(isAuthenticated: Boolean, isOnboarded: Boolean) {
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(
            when {
                !isAuthenticated -> R.id.loginFragment
                !isOnboarded -> R.id.welcomeFragment
                else -> R.id.homeTransitionFragment
            }
        )
        navController.graph = graph

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.daily_progress_fragment,
                R.id.recipeFragment,
                R.id.dashboardFragment,
                R.id.loginFragment
            )
        )

        val toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = when (destination.id) {
                R.id.loginFragment,
                R.id.welcomeFragment,
                R.id.goalSelectionFragment -> true
                else -> false
            }

            if (isAuthScreen) {
                viewModel.setBottomNavVisibility(false)
                supportActionBar?.hide()
            } else {
                // For non-auth screens, visibility is determined by permission state
                val isPermissionGranted = viewModel.uiState.value.isActivityPermissionGranted
                viewModel.setBottomNavVisibility(isPermissionGranted)
                if (destination.id == R.id.homeTransitionFragment) {
                    supportActionBar?.hide()
                } else {
                    supportActionBar?.show()
                }
            }
        }
    }
}