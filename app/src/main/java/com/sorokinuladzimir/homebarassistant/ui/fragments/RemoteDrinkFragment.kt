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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.ui.drinkdetail.DrinkDetailScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.RemoteDrinkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemoteDrinkFragment : Fragment(), BackButtonListener {

    private val drinkState = mutableStateOf<Drink?>(null)
    private val ingredientsState = mutableStateOf<List<WholeCocktail>>(emptyList())
    private lateinit var viewModel: RemoteDrinkViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_detail_compose, container, false)
        initToolbar(rootView)

        val composeView = rootView.findViewById<ComposeView>(R.id.detail_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                DrinkDetailScreen(
                    drink = drinkState.value,
                    ingredients = ingredientsState.value,
                    showNotes = false,
                    onIngredientClick = {},
                    onImageLoaded = { viewModel.setBitmap(it) }
                )
            }
        }

        viewModel = ViewModelProvider(this)[RemoteDrinkViewModel::class.java]
        initFab(rootView)
        viewModel.getDrink().observe(viewLifecycleOwner) {
            drinkState.value = it
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it?.name
        }
        viewModel.getDrinkIngredients().observe(viewLifecycleOwner) {
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
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
        fab.setOnClickListener {
            fab.isEnabled = false
            viewModel.saveDrink()
            onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override fun onBackPressed(): Boolean {
        parentFragment?.let { (it as RouterProvider).router.exit() }
        return true
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_BUNDLE = "extra_bundle"

        @JvmStatic
        fun getNewInstance(name: String?, bundle: Bundle): RemoteDrinkFragment {
            val fragment = RemoteDrinkFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            arguments.putBundle(EXTRA_BUNDLE, bundle)
            fragment.arguments = arguments
            return fragment
        }
    }
}
