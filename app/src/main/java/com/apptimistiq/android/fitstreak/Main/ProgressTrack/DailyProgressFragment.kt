package com.apptimistiq.android.fitstreak.Main.ProgressTrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.apptimistiq.android.fitstreak.databinding.FragmentDailyProgressBinding


class DailyProgressFragment : Fragment() {

    private lateinit var binding: FragmentDailyProgressBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDailyProgressBinding.inflate(layoutInflater, container, false)
        return binding.root

    }


}







