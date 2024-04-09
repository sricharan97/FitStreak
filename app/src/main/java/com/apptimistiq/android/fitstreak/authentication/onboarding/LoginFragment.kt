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


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<AuthenticationViewModel> { viewModelFactory }

    companion object {
        const val TAG = "Login Fragment"
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.authenticationComponent().create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //get instance of the binding class using static inflate method
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        // Inflate the layout for this fragment
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.helloButton.setOnClickListener {
            launchSignInFlow()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.signInFlowStatus.collect {
                    if (it) {
                        if (viewModel.userState.value.isOnboarded) {
                            navigateHomeAfterSuccessfulLogin()
                        } else {
                            navigateOnboardingFlow()
                        }

                        viewModel.signInFlowReset()
                    }
                }

            }
        }

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

            viewModel.signInFlowCompleted()

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

        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainActivity())

    }

    //Navigate to On boarding flow for new users
    private fun navigateOnboardingFlow() {

        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToWelcomeFragment())

    }
}