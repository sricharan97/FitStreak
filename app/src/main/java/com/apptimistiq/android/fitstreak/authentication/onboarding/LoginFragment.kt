package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LoginFragment handles the authentication flow for the application.
 *
 * This fragment:
 * 1. Presents the initial login screen to the user
 * 2. Launches the FirebaseUI authentication flow when the user clicks the login button
 * 3. Handles authentication results and navigates to appropriate destinations based on
 *    whether the user has completed the onboarding process
 *
 * Uses Firebase Authentication UI library for handling different authentication methods.
 */
class LoginFragment : Fragment() {

    //region Properties
    /** View binding for the login fragment layout */
    private lateinit var binding: FragmentLoginBinding
    
    /** Flag to track if user has completed onboarding */
    private var completedOnboarding: Boolean = false

    /** ViewModel factory provided by Dagger DI */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /** Shared authentication view model */
    private val viewModel by activityViewModels<AuthenticationViewModel> { viewModelFactory }
    
    /** Contract for handling Firebase Auth UI results */
    private val signInLauncher = 
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }
    //endregion

    //region Constants
    companion object {
        /** Tag for logging purposes */
        const val TAG = "LoginFragment"
    }
    //endregion

    //region Lifecycle Methods
    /**
     * Injects dependencies when fragment is attached to activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.loginComponent().create()
            .inject(this)
    }

    /**
     * Inflates the fragment layout and initializes data binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    /**
     * Sets up UI interactions and observes view model state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeUserState()
    }
    //endregion

    //region UI Setup
    /**
     * Sets up click listeners for UI elements
     */
    private fun setupClickListeners() {
        binding.helloButton.setOnClickListener {
            launchSignInFlow()
        }
    }

    /**
     * Observes user state to determine if onboarding is completed
     */
    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect {
                    if (it.isOnboarded) {
                        completedOnboarding = true
                    }
                }
            }
        }
    }
    //endregion

    //region Authentication
    /**
     * Initiates the Firebase authentication UI flow
     *
     * Configures available authentication providers (Email, Phone, Google)
     * and launches the authentication UI with a custom layout
     */
    private fun launchSignInFlow() {
        // Configure authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Set up custom authentication layout
        val authMethodPickerLayout = AuthMethodPickerLayout
            .Builder(R.layout.custom_login_layout)
            .setGoogleButtonId(R.id.google_sign_in_button)
            .setEmailButtonId(R.id.sign_in_email_button)
            .setPhoneButtonId(R.id.phone_sign_in_button)
            .build()

        // Build and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.Theme_FitStreak)
            .build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Handles the result of the sign-in flow
     *
     * Based on the authentication result and the user's onboarding status,
     * navigates to the appropriate destination
     *
     * @param result The Firebase authentication result
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        
        if (result.resultCode == RESULT_OK) {
            if (completedOnboarding) {
                Log.d(TAG, "Inside the SignInResult success block")
                navigateHomeAfterSuccessfulLogin()
            } else {
                navigateOnboardingFlow()
            }
        } else {
            Log.e(
                TAG,
                "Error in Sign in flow with following error code - ${response?.error?.errorCode}"
            )
        }
    }
    //endregion

    //region Navigation
    /**
     * Navigates to the main activity after successful authentication
     * for users who have already completed onboarding
     */
    private fun navigateHomeAfterSuccessfulLogin() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainActivity())
        Log.d(TAG, "Navigation to home fragment in trigger is called")
        requireActivity().finish()
        Log.d(TAG, "Activity finish called")
    }

    /**
     * Navigates to the onboarding flow for new users who need to complete setup
     */
    private fun navigateOnboardingFlow() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToWelcomeFragment())
    }
    //endregion
}
