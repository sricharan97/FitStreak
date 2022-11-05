package com.apptimistiq.android.fitstreak.main.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.databinding.RecipeChildItemBinding
import com.apptimistiq.android.fitstreak.main.data.domain.Recipe

class RecipeListAdapter(val clickListener: RecipeItemListener) :
    ListAdapter<Recipe, RecyclerView.ViewHolder>(RecipeItemDiffCallback()) {

    class ViewHolder private constructor(val binding: RecipeChildItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: RecipeItemListener, item: Recipe) {
            binding.recipe = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {

            fun from(parent: ViewGroup): ViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipeChildItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(clickListener, getItem(position) as Recipe)
        }
    }


}


/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between old list and a new
 * list that's been passed to `submitList`.
 */

class RecipeItemDiffCallback : DiffUtil.ItemCallback<Recipe>() {

    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem.id == newItem.id
    }
}


class RecipeItemListener(val RecipeClickListener: (recipe: Recipe) -> Unit) {

    fun onClick(recipeItem: Recipe) = RecipeClickListener(recipeItem)
}