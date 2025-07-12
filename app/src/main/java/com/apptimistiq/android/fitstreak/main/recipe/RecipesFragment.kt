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

        return binding.root
    }

    /**
     * Sets up UI components, adapters, and observes view model data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDataBinding()
        setupRecyclerView()
        setupChips()
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

        // Set layout manager based on orientation
        val isLandscape = resources.getBoolean(R.bool.is_landscape)
        if (isLandscape) {
            binding.recipeParentRecyclerView.layoutManager =
                androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
        } else {
            binding.recipeParentRecyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }
    }

    private fun setupChips() {
        // Set chip click listeners
        binding.chipVegetarian.setOnClickListener {
            viewModel.updateMenuDietType(RecipeDietType.Vegetarian)
        }

        binding.chipVegan.setOnClickListener {
            viewModel.updateMenuDietType(RecipeDietType.Vegan)
        }

        binding.chipPaleo.setOnClickListener {
            viewModel.updateMenuDietType(RecipeDietType.Paleo)
        }

        binding.chipKeto.setOnClickListener {
            viewModel.updateMenuDietType(RecipeDietType.Keto)
        }


    }

    private fun updateSelectedChip(dietType: String) {
        with(binding) {
            chipVegetarian.isChecked = dietType == recipeTypeMap[RecipeDietType.Vegetarian]
            chipVegan.isChecked = dietType == recipeTypeMap[RecipeDietType.Vegan]
            chipPaleo.isChecked = dietType == recipeTypeMap[RecipeDietType.Paleo]
            chipKeto.isChecked = dietType == recipeTypeMap[RecipeDietType.Keto]
        }
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


        // Observe diet type changes for chip selection
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentDietType.collect { dietType ->
                    updateSelectedChip(dietType)
                }
            }
        }

        // Observe loading state to control shimmer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        binding.shimmerLayout.startShimmer()
                    } else {
                        binding.shimmerLayout.stopShimmer()
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


}
