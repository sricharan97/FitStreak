package com.example.android.fitstreak.Authentication.Onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.android.fitstreak.databinding.FragmentWelcomeBinding

class WelcomeFragment : androidx.fragment.app.Fragment() {


    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //get instance of the binding class using static inflate method
        binding = FragmentWelcomeBinding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.navigateNextButton.setOnClickListener {
            navigateToGoalSelection()
        }

    }


    //Navigate to GoalSelection screen
    private fun navigateToGoalSelection() {

        findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToGoalSelectionFragment())

    }
}