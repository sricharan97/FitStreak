package com.apptimistiq.android.fitstreak.main.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.databinding.RecipeChildItemBinding
import com.apptimistiq.android.fitstreak.main.data.domain.Recipe

/**
 * Adapter for displaying recipe items in a horizontal RecyclerView.
 *
 * This adapter uses [ListAdapter] with [DiffUtil] for efficient item updates
 * and Data Binding to bind recipe data to the layout.
 *
 * @property clickListener Listener for handling recipe item click events
 */
class RecipeListAdapter(val clickListener: RecipeItemListener) :
    ListAdapter<Recipe, RecyclerView.ViewHolder>(RecipeItemDiffCallback()) {

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
            is ViewHolder -> holder.bind(clickListener, getItem(position) as Recipe)
        }
    }

    /**
     * ViewHolder class for recipe items with binding functionality.
     */
    class ViewHolder private constructor(val binding: RecipeChildItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds recipe data to the view and sets up click listener.
         *
         * @param clickListener The listener that handles click events
         * @param item The recipe item to bind
         */
        fun bind(clickListener: RecipeItemListener, item: Recipe) {
            binding.recipe = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
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
                val binding = RecipeChildItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 * DiffUtil callback for calculating differences between Recipe items.
 *
 * Determines whether two recipes are the same item and whether they have the same content,
 * enabling efficient updates with RecyclerView's ListAdapter.
 */
class RecipeItemDiffCallback : DiffUtil.ItemCallback<Recipe>() {

    /**
     * Checks whether two recipes represent the same item.
     *
     * @param oldItem The previous recipe item
     * @param newItem The new recipe item
     * @return True if they represent the same item (identity check)
     */
    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem === newItem
    }

    /**
     * Checks whether two recipes have the same content.
     *
     * @param oldItem The previous recipe item
     * @param newItem The new recipe item
     * @return True if they have the same content (equality check on ID)
     */
    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem.id == newItem.id
    }
}

/**
 * Click listener class for recipe items.
 *
 * Uses a lambda function to handle click events on recipe items.
 *
 * @property RecipeClickListener Lambda that receives the clicked recipe item
 */
class RecipeItemListener(val RecipeClickListener: (recipe: Recipe) -> Unit) {
    /**
     * Click event handler that delegates to the provided lambda.
     *
     * @param recipeItem The recipe item that was clicked
     */
    fun onClick(recipeItem: Recipe) = RecipeClickListener(recipeItem)
}
