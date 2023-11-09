package com.apptimistiq.android.fitstreak.main.dashboard

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
import com.apptimistiq.android.fitstreak.databinding.FragmentGoalEditBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val LOG_TAG = "GoalEditFragment"


class GoalEditFragment : Fragment() {

    private lateinit var binding: FragmentGoalEditBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<DashboardViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.GoalEditComponent().create()
            .inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_goal_edit, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goalInfoVal.collect { goalInfoVal ->
                    viewModel.updateDisplayedGoalInfoVal(goalInfoVal)

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateBackToDashboard.collect {
                    if (it) {
                        findNavController().navigate(R.id.action_goalEditFragment_to_dashboard_dest)
                        viewModel.navigateBackDashboardFragmentComplete()
                    }
                }
            }
        }

        when (arguments?.get("info_type")) {

            GoalUserInfo.STEPS -> {
                binding.editType.text = resources.getString(R.string.step_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.step_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.step_goal_tag)
            }

            GoalUserInfo.EXERCISE -> {
                binding.editType.text = resources.getString(R.string.exercise_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.exercise_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.exercise_goal_tag)
            }

            GoalUserInfo.SLEEP -> {
                binding.editType.text = resources.getString(R.string.sleep_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.sleep_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.sleep_goal_tag)
            }

            GoalUserInfo.WATER -> {
                binding.editType.text = resources.getString(R.string.water_text_value)
                binding.editTypeDescp.text = resources.getString(
                    R.string.water_goal_edit_description
                )
                binding.goalTag.text = resources.getString(R.string.water_goal_tag)
            }

            GoalUserInfo.HEIGHT -> {
                binding.editType.text = resources.getString(R.string.height_info)
                binding.editTypeDescp.visibility = View.INVISIBLE
                binding.goalTag.text = resources.getString(R.string.height_info_tag)
            }

            GoalUserInfo.WEIGHT -> {
                binding.editType.text = resources.getString(R.string.weight_info)
                binding.editTypeDescp.visibility = View.INVISIBLE
                binding.goalTag.text = resources.getString(R.string.weight_info_tag)
            }


        }
    }


}