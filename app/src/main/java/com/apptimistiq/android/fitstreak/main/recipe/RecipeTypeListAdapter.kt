package com.apptimistiq.android.fitstreak.main.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.databinding.RecipeParentItemBinding
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState

/**
 * Adapter for displaying recipe categories in a vertical RecyclerView.
 *
 * Each item in this adapter represents a recipe category that contains a horizontal
 * RecyclerView of recipes managed by [RecipeListAdapter]. This adapter uses [ListAdapter]
 * with [DiffUtil] for efficient updates and Data Binding to bind UI state to the layout.
 *
 * @property clickListener Listener for handling recipe item click events
 */
class RecipeTypeListAdapter(val clickListener: RecipeItemListener) :
    ListAdapter<RecipeTrackUiState, RecyclerView.ViewHolder>(RecipeParentItemDiffCallback()) {

    /**
     * Creates a new ViewHolder when there are no existing ones to reuse.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder which should be updated with data
     * @param position The position of the item in the data set
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(clickListener, getItem(position) as RecipeTrackUiState)
        }
    }

    /**
     * ViewHolder class for recipe category items with nested RecyclerView functionality.
     */
    class ViewHolder private constructor(val binding: RecipeParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds recipe category data to the view and sets up the nested RecyclerView.
         *
         * @param clickListener The listener for handling recipe item clicks
         * @param item The recipe category UI state to bind
         */
        fun bind(clickListener: RecipeItemListener, item: RecipeTrackUiState) {
            binding.recipeTrackUiState = item

            // Initialize child RecyclerView with horizontal layout
            val recipeChildAdapter = RecipeListAdapter(clickListener)
            binding.recipeChildRecyclerView.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL, 
                false
            )
            binding.recipeChildRecyclerView.adapter = recipeChildAdapter
            // Note: Need to submit list to adapter here
        }

        companion object {
            /**
             * Factory method to create a new ViewHolder.
             *
             * @param parent The ViewGroup into which the new View will be added
             * @return A new ViewHolder instance
             */
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipeParentItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 * DiffUtil callback for calculating differences between RecipeTrackUiState items.
 *
 * Determines whether two recipe categories are the same item and whether they have
 * the same content, enabling efficient updates with RecyclerView's ListAdapter.
 */
class RecipeParentItemDiffCallback : DiffUtil.ItemCallback<RecipeTrackUiState>() {

    /**
     * Checks whether two recipe categories represent the same item.
     *
     * @param oldItem The previous recipe category item
     * @param newItem The new recipe category item
     * @return True if they represent the same item (identity check)
     */
    override fun areItemsTheSame(
        oldItem: RecipeTrackUiState,
        newItem: RecipeTrackUiState
    ): Boolean {
        return oldItem === newItem
    }

    /**
     * Checks whether two recipe categories have the same content.
     *
     * @param oldItem The previous recipe category item
     * @param newItem The new recipe category item
     * @return True if they have the same content (equality check on recipe type)
     */
    override fun areContentsTheSame(
        oldItem: RecipeTrackUiState,
        newItem: RecipeTrackUiState
    ): Boolean {
        return oldItem.recipeType == newItem.recipeType
    }
}
