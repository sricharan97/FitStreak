package com.apptimistiq.android.fitstreak.main.progressTrack

import android.content.Context
import android.os.Bundle
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
import com.apptimistiq.android.fitstreak.databinding.FragmentEditActivityBinding
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class EditActivityFragment : Fragment() {

    private lateinit var binding: FragmentEditActivityBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<ProgressViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {

        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.EditActivityComponent().create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_activity, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        when (arguments?.get("act_type")) {

            ActivityType.STEP -> {
                binding.activityEditType.text = resources.getString(R.string.edit_steps)
                binding.activityValTag.text = resources.getString(R.string.edit_steps_tag)
            }

            ActivityType.SLEEP -> {
                binding.activityEditType.text = resources.getString(R.string.edit_sleep)
                binding.activityValTag.text = resources.getString(R.string.edit_sleep_tag)
            }

            ActivityType.EXERCISE -> {
                binding.activityEditType.text = resources.getString(R.string.edit_exercise)
                binding.activityValTag.text = resources.getString(R.string.edit_exercise_tag)
            }

            ActivityType.WATER -> {
                binding.activityEditType.text = resources.getString(R.string.edit_water)
                binding.activityValTag.text = resources.getString(R.string.edit_water_tag)
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityValueCurrent.collect {
                    viewModel.updateDisplayedActivityVal(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateBackProgress.collect {
                    if (it) {
                        findNavController().navigate(R.id.action_editActivityFragment_to_home_dest)
                        viewModel.navigateBackToProgressFragmentCompleted()
                    }
                }
            }
        }


    }
}