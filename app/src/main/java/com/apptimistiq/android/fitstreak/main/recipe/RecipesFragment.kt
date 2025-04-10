package com.apptimistiq.android.fitstreak.main.recipe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

/**
 * Fragment that displays a list of recipes categorized by types.
 * 
 * This fragment handles:
 * - Displaying recipe categories using RecyclerView
 * - Handling diet type filtering via menu options
 * - Opening recipe details in web browser
 * - Communication with RecipeViewModel for data management
 */
class RecipesFragment : Fragment() {

    // region Properties

    private lateinit var binding: FragmentRecipesBinding
    private lateinit var recyclerAdapter: RecipeTypeListAdapter

    // Dagger injection of ViewModelFactory
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<RecipeViewModel> { viewModelFactory }

    // endregion

    // region Lifecycle Methods

    /**
     * Handles dependency injection when fragment attaches to context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as FitApp).appComponent.recipesTrackComponent().create()
            .inject(this)
    }

    /**
     * Inflates the layout and initializes data binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_recipes,
            container, false
        )

        setHasOptionsMenu(true)

        return binding.root
    }

    /**
     * Sets up UI components, adapters, and observes view model data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDataBinding()
        setupRecyclerView()
        observeViewModel()
    }

    // endregion

    // region Setup Methods

    /**
     * Configures data binding with lifecycle owner and view model
     */
    private fun setupDataBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    /**
     * Sets up the recipe recycler view with adapter and click listener
     */
    private fun setupRecyclerView() {
        recyclerAdapter = RecipeTypeListAdapter(RecipeItemListener { recipe ->
            viewModel.updateCurrentRecipeId(recipe.id)
        })

        binding.recipeParentRecyclerView.adapter = recyclerAdapter
    }

    /**
     * Observes view model state for navigation events
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipeUrl.collect { recipeUrl ->
                    recipeUrl?.let {
                        openRecipeInBrowser(it)
                    }
                }
            }
        }
    }

    /**
     * Opens the given URL in device's browser
     */
    private fun openRecipeInBrowser(recipeUrl: String) {
        val url = Uri.parse(recipeUrl)
        val intent = Intent(Intent.ACTION_VIEW, url)
        startActivity(intent)
        viewModel.navigateToRecipeUrlCompleted()
    }

    // endregion

    // region Menu Handling

    /**
     * Inflates the options menu for diet filtering
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipe_diet_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handles selection of diet type filter options
     */
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

    // endregion
}
