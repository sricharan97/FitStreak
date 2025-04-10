package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentWelcomeBinding

/**
 * Welcome screen fragment that introduces the app to the user.
 * This is the first screen in the onboarding flow.
 */
class WelcomeFragment : Fragment() {

    // View binding instance
    private lateinit var binding: FragmentWelcomeBinding

    /**
     * Inflates the fragment layout and initializes data binding.
     *
     * @param inflater The layout inflater
     * @param container The parent view group
     * @param savedInstanceState Previously saved state of the fragment
     * @return The inflated view
     */
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the binding object using DataBindingUtil
        binding = DataBindingUtil.inflate(
            inflater, 
            R.layout.fragment_welcome, 
            container, 
            false
        )
        
        return binding.root
    }

    /**
     * Called immediately after onCreateView() has returned, but before any
     * saved state has been restored in to the view.
     *
     * @param view The view returned by onCreateView
     * @param savedInstanceState Previously saved state of the fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup navigation to next screen
        binding.navigateNextButton.setOnClickListener {
            navigateToGoalSelection()
        }
    }

    /**
     * Navigates to the Goal Selection screen.
     * Uses the NavController to navigate via the predefined action.
     */
    private fun navigateToGoalSelection() {
        findNavController().navigate(
            WelcomeFragmentDirections.actionWelcomeFragmentToGoalSelectionFragment()
        )
    }
}
