package com.apptimistiq.android.fitstreak.main.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.databinding.RecipeParentItemBinding
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState

class RecipeTypeListAdapter(val clickListener: RecipeItemListener) :
    ListAdapter<RecipeTrackUiState, RecyclerView.ViewHolder>(RecipeParentItemDiffCallback()) {

    class ViewHolder private constructor(val binding: RecipeParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: RecipeItemListener, item: RecipeTrackUiState) {
            binding.recipeTrackUiState = item

            val recipeChildAdapter = RecipeListAdapter(clickListener)
            binding.recipeChildRecyclerView.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL, false
            )
            binding.recipeChildRecyclerView.adapter = recipeChildAdapter

        }

        companion object {

            fun from(parent: ViewGroup): ViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipeParentItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ViewHolder -> holder.bind(clickListener, getItem(position) as RecipeTrackUiState)
        }

    }


}


/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between old list and a new
 * list that's been passed to `submitList`.
 */

class RecipeParentItemDiffCallback : DiffUtil.ItemCallback<RecipeTrackUiState>() {

    override fun areItemsTheSame(
        oldItem: RecipeTrackUiState,
        newItem: RecipeTrackUiState
    ): Boolean {

        return oldItem === newItem

    }

    override fun areContentsTheSame(
        oldItem: RecipeTrackUiState,
        newItem: RecipeTrackUiState
    ): Boolean {

        return oldItem.recipeType == newItem.recipeType
    }
}