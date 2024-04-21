package com.apptimistiq.android.fitstreak.main.dashboard


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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
import com.apptimistiq.android.fitstreak.databinding.FragmentDashboardBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<DashboardViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.DashboardComponent().create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToEditGoal.collect {
                    if (it != GoalUserInfo.DEFAULT) {
                        val bundle = bundleOf("info_type" to it)
                        findNavController().navigate(
                            R.id.action_dashboard_dest_to_goalEditFragment,
                            bundle
                        )
                        viewModel.navigateToEditGoalCompleted()
                    }
                }
            }
        }
    }
}