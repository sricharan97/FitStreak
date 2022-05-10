package com.example.android.fitstreak.Onboarding

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.fitstreak.R
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    companion object {
        const val TAG = "Login Fragment"
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchSignInFlow()


    }


    //Trigger the sign in flow
    private fun launchSignInFlow() {

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //Custom AuthMethodPickerLayout
        val authMethodPickerLayout = AuthMethodPickerLayout
            .Builder(R.layout.custom_login_layout)
            .setGoogleButtonId(R.id.google_sign_in_button)
            .setEmailButtonId(R.id.sign_in_email_button)
            .setPhoneButtonId(R.id.phone_sign_in_button)
            .build()


        // Create sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.Theme_FitStreak)
            .build()

        //signInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //Launch sign-inIntent
        signInLauncher.launch(signInIntent)

    }

    //Handle once sign-in flow completes
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {

        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            //TODO: Handle the flow after successful sign in
            navigateOnboardingFlow()
        } else {
            //TODO: Sign in failed, If response is null the user canceled the
            //    // sign-in flow using the back button. Otherwise check
            //    // response.getError().getErrorCode() and handle the error
            Log.e(
                TAG,
                "Error in Sign in flow with following error code - ${response?.error?.errorCode}"
            )

        }

    }

    //navigate to Home_Dest
    private fun navigateHomeAfterSuccessfulLogin() {

        findNavController().navigate(LoginFragmentDirections.actionGlobalHomeDest2())

    }

    //Navigate to On boarding flow for new users
    private fun navigateOnboardingFlow() {

        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToOnboardingFlow())

    }
}