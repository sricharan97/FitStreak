package com.apptimistiq.android.fitstreak.main.recipe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentRecipesBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class RecipesFragment : Fragment() {

    private lateinit var binding: FragmentRecipesBinding

    private lateinit var recyclerAdapter: RecipeTypeListAdapter

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModels<RecipeViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.recipesTrackComponent().create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_recipes,
            container, false
        )

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipeUrl.collect { recipeUrl ->

                    recipeUrl?.let {
                        val url = Uri.parse(recipeUrl)
                        val intent = Intent(Intent.ACTION_VIEW, url)
                        startActivity(intent)
                        viewModel.navigateToRecipeUrlCompleted()
                    }
                }
            }
        }

        recyclerAdapter = RecipeTypeListAdapter(RecipeItemListener { recipe ->
            viewModel.updateCurrentRecipeId(recipe.id)

        })

        binding.recipeParentRecyclerView.adapter = recyclerAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipe_diet_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.vegetarian_diet -> {
                viewModel.updateMenuDietType(RecipeDietType.Vegetarian)
                true
            }

            R.id.vegan_diet -> {
                viewModel.updateMenuDietType(RecipeDietType.Vegan)
                true
            }

            R.id.paleo_diet -> {
                viewModel.updateMenuDietType(RecipeDietType.Paleo)
                true
            }

            R.id.keto_diet -> {
                viewModel.updateMenuDietType(RecipeDietType.Keto)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }


    }
}