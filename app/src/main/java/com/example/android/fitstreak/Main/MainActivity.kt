package com.example.android.fitstreak.Main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.android.fitstreak.R
import com.example.android.fitstreak.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding

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

        //TODO: When the MainActivity launches, check if the user is a registered user and navigate him
        //accordingly to the login and Onboarding screens.

    }


}