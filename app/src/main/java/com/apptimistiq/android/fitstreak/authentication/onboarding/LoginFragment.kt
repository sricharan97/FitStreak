package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LoginFragment handles the authentication flow for the application.
 */
class LoginFragment : Fragment() {

    //region Properties
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

    /**
     * Flag to control whether to automatically launch sign-in flow.
     * Set to false during tests to avoid timing issues.
     */
    @VisibleForTesting
    internal var shouldAutoLaunchSignIn = true
    //endregion

    //region Constants
    companion object {
        /** Tag for logging purposes */
        const val TAG = "LoginFragment"
    }
    //endregion

    //region Lifecycle Methods
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.loginComponent().create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Return a simple FrameLayout. FirebaseUI will draw over it.
        val frameLayout = FrameLayout(requireContext())
        frameLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return frameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Only launch sign-in flow automatically if not in test mode
        if (shouldAutoLaunchSignIn) {
            launchSignInFlow()
        }
    }
    //endregion

    //region Authentication
    /**
     * Initiates the Firebase authentication UI flow
     */
    @VisibleForTesting
    internal fun launchSignInFlow() {
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
            .setIsSmartLockEnabled(false,true) //Attempt to force Auth picker to show
            .build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Handles the result of the sign-in flow
     *
     * @param result The Firebase authentication result
     */
    @VisibleForTesting
    internal fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            viewLifecycleOwner.lifecycleScope.launch {
                try{
                    val userStateInfo = viewModel.finalizeAuthentication() // Fetches updated UserStateInfo
                    if (userStateInfo.isOnboarded) {
                        Log.d(TAG, "Sign-in successful. User already onboarded. Navigating to home.")
                        navigateHomeAfterSuccessfulLogin()
                    } else {
                        Log.d(TAG, "Sign-in successful. User needs onboarding. Navigating to welcome.")
                        navigateOnboardingFlow()
                    }
                }catch (e:Exception) {
                    Log.e(TAG, "Error in finalizing authentication: ${e.message}")
                    // Handle error (e.g., show a message to the user)
                }
            }
        } else {
            // This block is executed when the user presses the back button from the
            // sign-in screen or if there is an error.
            Log.w(TAG, "Sign-in failed or was cancelled. Error code: ${response?.error?.errorCode}")

            // To prevent showing the blank LoginFragment, we attempt to pop the back stack.
            val popped = findNavController().popBackStack()
            // If popBackStack() returns false, it means this was the last fragment on the stack.
            // In this case, we should finish the activity to exit the app.
            if (!popped) {
                requireActivity().finish()
            }
        }
    }
    //endregion

    //region Navigation
    private fun navigateHomeAfterSuccessfulLogin() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeTransitionFragment())
        Log.d(TAG, "Navigation to home transition fragment is called")
    }

    private fun navigateOnboardingFlow() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToWelcomeFragment())
    }
    //endregion
}