package com.apptimistiq.android.fitstreak.main.progressTrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.RecyclerItemLayoutBinding
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType

/**
 * Adapter for rendering activity items in a RecyclerView.
 * 
 * This adapter uses [ListAdapter] to efficiently update the list when data changes,
 * with the help of [ActivityItemDiffCallback] to determine changes between items.
 *
 * @property clickListener Listener to handle item click events
 */
class ActivityListAdapter(val clickListener: ActivityItemListener) :
    ListAdapter<ActivityItemUiState, RecyclerView.ViewHolder>(ActivityItemDiffCallback()) {

    /**
     * ViewHolder for activity items that binds data to the list item view.
     *
     * @property binding The data binding object for the list item view
     */
    class ViewHolder private constructor(val binding: RecyclerItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the activity item data to the view and sets up click handlers.
         *
         * @param clickListener The listener to handle item clicks
         * @param item The activity item data to bind
         */
        fun bind(clickListener: ActivityItemListener, item: ActivityItemUiState) {
            binding.activityItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            /**
             * Factory method to create a new ViewHolder instance.
             *
             * @param parent The parent ViewGroup
             * @return A new ViewHolder instance
             */
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerItemLayoutBinding
                    .inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    /**
     * Creates new ViewHolder instances as needed.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     * Binds the data at the specified position to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(clickListener, getItem(position) as ActivityItemUiState)
        }
    }
}

/**
 * DiffUtil callback for calculating differences between activity item lists.
 *
 * This helps RecyclerView efficiently update only the items that have changed
 * instead of refreshing the entire list.
 */
class ActivityItemDiffCallback : DiffUtil.ItemCallback<ActivityItemUiState>() {
    /**
     * Determines if two objects represent the same item.
     *
     * @param oldItem The old item in the list
     * @param newItem The new item in the list
     * @return True if they represent the same item, false otherwise
     */
    override fun areItemsTheSame(
        oldItem: ActivityItemUiState,
        newItem: ActivityItemUiState
    ): Boolean {
        return oldItem.dataType == newItem.dataType
    }

    /**
     * Determines if the contents of two items are the same.
     *
     * @param oldItem The old item in the list
     * @param newItem The new item in the list
     * @return True if the contents are the same, false otherwise
     */
    override fun areContentsTheSame(
        oldItem: ActivityItemUiState,
        newItem: ActivityItemUiState
    ): Boolean {
        return ((oldItem.currentReading == newItem.currentReading) &&
                oldItem.goalReading == newItem.goalReading)
    }
}

/**
 * Listener class to handle click events on activity items.
 *
 * @property activityClickListener Lambda function to handle the click event
 */
class ActivityItemListener(val activityClickListener: (activityType: ActivityType) -> Unit) {
    /**
     * Called when an activity item is clicked.
     *
     * @param activityItem The activity item that was clicked
     */
    fun onClick(activityItem: ActivityItemUiState) = activityClickListener(activityItem.dataType)
}
