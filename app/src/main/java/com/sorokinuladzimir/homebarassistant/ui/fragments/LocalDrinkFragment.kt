package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.ui.drinkdetail.DrinkDetailScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.DrinkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocalDrinkFragment : Fragment(), BackButtonListener {

    private val drinkState = mutableStateOf<Drink?>(null)
    private val ingredientsState = mutableStateOf<List<WholeCocktail>>(emptyList())
    private var drinkId: Long = -1L
    private var editable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        drinkId = arguments?.getLong(EXTRA_ID) ?: -1L
        editable = arguments?.getBoolean(EXTRA_EDITABLE) ?: false

        val rootView = inflater.inflate(R.layout.fr_detail_compose, container, false)
        initToolbar(rootView)
        initFab(rootView)

        val composeView = rootView.findViewById<ComposeView>(R.id.detail_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                DrinkDetailScreen(
                    drink = drinkState.value,
                    ingredients = ingredientsState.value,
                    showNotes = true,
                    onIngredientClick = { navigateToIngredient(it) }
                )
            }
        }

        val viewModel = ViewModelProvider(this)[DrinkViewModel::class.java]
        viewModel.getDrink().observe(viewLifecycleOwner) {
            drinkState.value = it
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it?.name
        }
        viewModel.getIngredients().observe(viewLifecycleOwner) {
            ingredientsState.value = it ?: emptyList()
        }
        return rootView
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initFab(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        if (editable) {
            fab.setOnClickListener { navigateToEdit() }
        } else {
            fab.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    private fun navigateToEdit() {
        parentFragment?.let {
            (it as RouterProvider).router.navigateTo(Screens.ADD_DRINK, drinkId)
        }
    }

    private fun navigateToIngredient(ingredient: WholeCocktail) {
        parentFragment?.let {
            val bundle = Bundle()
            bundle.putLong(Constants.Extra.EXTRA_ID, ingredient.ingredientId ?: -1L)
            bundle.putBoolean(Constants.Extra.EDITABLE, false)
            (it as RouterProvider).router.navigateTo(Screens.LOCAL_INGREDIENT, bundle)
        }
    }

    override fun onBackPressed(): Boolean {
        parentFragment?.let { (it as RouterProvider).router.exit() }
        return true
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_ID = "extra_id"
        private const val EXTRA_EDITABLE = "extra_editable"

        @JvmStatic
        fun getNewInstance(name: String?, bundle: Bundle?): LocalDrinkFragment {
            val fragment = LocalDrinkFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            arguments.putLong(EXTRA_ID, bundle?.getLong(Constants.Extra.EXTRA_ID) ?: -1L)
            arguments.putBoolean(EXTRA_EDITABLE, bundle?.getBoolean(Constants.Extra.EDITABLE) ?: false)
            fragment.arguments = arguments
            return fragment
        }
    }
}
