package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.ui.ingredients.IngredientsListScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.IngredientListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IngredientsListFragment : Fragment() {

    private val ingredientsState = mutableStateOf<List<Ingredient>>(emptyList())
    private lateinit var viewModel: IngredientListViewModel
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_list_compose, container, false)
        initToolbar(rootView)
        initFAB(rootView)

        val composeView = rootView.findViewById<ComposeView>(R.id.list_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                IngredientsListScreen(
                    ingredients = ingredientsState.value,
                    emptyMessage = stringResource(R.string.no_items_ingredients),
                    onItemClick = { navigateToIngredient(it) }
                )
            }
        }

        val swipeRefresh = rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefresh.setOnRefreshListener { swipeRefresh.isRefreshing = false }

        viewModel = ViewModelProvider(requireActivity())[IngredientListViewModel::class.java]
        viewModel.restoreSources()
        viewModel.getIngredients().observe(viewLifecycleOwner) {
            ingredientsState.value = it ?: emptyList()
        }
        return rootView
    }

    private fun navigateToIngredient(ingredient: Ingredient) {
        parentFragment?.let {
            val bundle = Bundle()
            bundle.putLong(Constants.Extra.EXTRA_ID, ingredient.id ?: -1L)
            bundle.putBoolean(Constants.Extra.EDITABLE, true)
            (it as RouterProvider).router.navigateTo(Screens.LOCAL_INGREDIENT, bundle)
        }
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setTitle(R.string.ingredients_list_toolbar_title)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu)
        val actionMenuItem = menu.findItem(R.id.action_search)
        searchView = actionMenuItem.actionView as SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.searchIngredients(query)
                searchView?.let {
                    if (!it.isIconified) it.isIconified = true
                }
                actionMenuItem.collapseActionView()
                viewModel.restoreSources()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.searchIngredients(newText)
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(
                    Screens.ABOUT,
                    "Found drinks fragment anbout text"
                )
            }
        }
        if (item.itemId == R.id.action_settings) {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(Screens.SETTINGS)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initFAB(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
        fab.setOnClickListener {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(Screens.ADD_INGREDIENT, null)
            }
        }
    }

    companion object {
        private const val EXTRA_NAME = "ilf_extra_name"

        @JvmStatic
        fun getNewInstance(name: String?): IngredientsListFragment {
            val fragment = IngredientsListFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            fragment.arguments = arguments
            return fragment
        }
    }
}
