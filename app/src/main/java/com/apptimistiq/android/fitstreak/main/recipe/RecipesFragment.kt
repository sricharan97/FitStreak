package com.apptimistiq.android.fitstreak.main.recipe

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.apptimistiq.android.fitstreak.FitApp
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.databinding.FragmentRecipesBinding
import javax.inject.Inject


class RecipesFragment : Fragment() {

    private lateinit var binding: FragmentRecipesBinding

    // @Inject annotated fields will be provided by Dagger
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<RecipeViewModel> { viewModelFactory }

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