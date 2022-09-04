package com.apptimistiq.android.fitstreak.main.progressTrack

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.databinding.RecyclerItemLayoutBinding
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType

class ActivityListAdapter(val clickListener: ActivityItemListener) :
    ListAdapter<ActivityItemUiState, RecyclerView.ViewHolder>(ActivityItemDiffCallback()) {

    class ViewHolder private constructor(val binding: RecyclerItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ActivityItemListener, item: ActivityItemUiState) {
            binding.activityItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerItemLayoutBinding
                    .inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ViewHolder.from(parent)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(clickListener, getItem(position) as ActivityItemUiState)
        }
    }
}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between old list and a new
 * list that's been passed to `submitList`.
 */

class ActivityItemDiffCallback : DiffUtil.ItemCallback<ActivityItemUiState>() {
    override fun areItemsTheSame(
        oldItem: ActivityItemUiState,
        newItem: ActivityItemUiState
    ): Boolean {

        return oldItem.dataType == newItem.dataType

    }

    override fun areContentsTheSame(
        oldItem: ActivityItemUiState,
        newItem: ActivityItemUiState
    ): Boolean {

        return ((oldItem.currentReading == newItem.currentReading) &&
                oldItem.goalReading == newItem.goalReading)
    }

}

class ActivityItemListener(val activityClickListener: (activityType: ActivityType) -> Unit) {

    fun onClick(activityItem: ActivityItemUiState) = activityClickListener(activityItem.dataType)
}