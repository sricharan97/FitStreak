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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as FitApp).appComponent.mainActivityComponent().create().inject(this)

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val host =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment

        navController = host.navController

        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.daily_progress_fragment,
                    R.id.recipe_dest,
                    R.id.dashboard_dest
                )
            )

        val toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)

        toolbar.setupWithNavController(navController, appBarConfiguration)

        bottomNav = activityMainBinding.bottomNavView

        bottomNav.setupWithNavController(navController)

        //observe for the permission denied event and show the snackbar to the user
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityPermissionDenied.collect { permissionDenied ->
                    if (permissionDenied) {
                        Snackbar.make(
                            activityMainBinding.root,
                            getString(R.string.activity_permission_denied),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(R.string.ok) {
                                //TODO: Degrade the app feature
                                viewModel.degradeHomeDestinationMenu()
                            }
                            .setAnchorView(bottomNav)
                            .show()
                    }
                    viewModel.resetActivityPermissionDenied()
                }
            }
        }

        //check if the app experience needs to be degraded based on acitivty permission denial
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

        //check if the app experience needs to be upgraded based on acitivty permission approval
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

    private fun degradeHomeDestinationMenu() {
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.daily_progress_fragment -> {
                    navController.navigate(R.id.homeTransitionFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.homeTransitionFragment) {
                                inclusive = false
                            }
                        })
                    true
                }
                R.id.recipe_dest -> {
                    navController.navigate(R.id.recipe_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.recipe_dest) {
                                inclusive = false
                            }
                        }
                    )

                    true
                }
                R.id.dashboard_dest -> {
                    navController.navigate(R.id.dashboard_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.dashboard_dest) {
                                inclusive = false
                            }
                        }
                    )

                    true
                }
                else -> false
            }

        }
    }

    private fun upgradeHomeDestinationMenu() {
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.daily_progress_fragment -> {
                    navController.navigate(R.id.daily_progress_fragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.daily_progress_fragment) {
                                inclusive = false
                            }
                        }
                    )

                    true
                }
                R.id.recipe_dest -> {
                    navController.navigate(R.id.recipe_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.recipe_dest) {
                                inclusive = false
                            }
                        })

                    true
                }
                R.id.dashboard_dest -> {
                    navController.navigate(R.id.dashboard_dest,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.dashboard_dest) {
                                inclusive = false
                            }
                        })
                    true
                }
                else -> false
            }

        }
    }
}












