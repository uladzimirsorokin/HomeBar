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
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.ui.drinks.DrinksListScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.DrinkListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DrinksListFragment : Fragment() {

    private val drinksState = mutableStateOf<List<Drink>>(emptyList())
    private lateinit var viewModel: DrinkListViewModel
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
                DrinksListScreen(
                    drinks = drinksState.value,
                    emptyMessage = stringResource(R.string.no_items_drinks),
                    onItemClick = { navigateToDrink(it) }
                )
            }
        }

        val swipeRefresh = rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefresh.setOnRefreshListener { swipeRefresh.isRefreshing = false }

        viewModel = ViewModelProvider(requireActivity())[DrinkListViewModel::class.java]
        viewModel.restoreSources()
        viewModel.getDrinks().observe(viewLifecycleOwner) {
            drinksState.value = it ?: emptyList()
        }
        return rootView
    }

    private fun navigateToDrink(drink: Drink) {
        parentFragment?.let {
            val bundle = Bundle()
            bundle.putLong(Constants.Extra.EXTRA_ID, drink.id ?: -1L)
            bundle.putBoolean(Constants.Extra.EDITABLE, true)
            (it as RouterProvider).router.navigateTo(Screens.LOCAL_DRINK, bundle)
        }
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setTitle(R.string.drinks_list_fragment_title)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu)
        val actionMenuItem = menu.findItem(R.id.action_search)
        searchView = actionMenuItem.actionView as SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.searchDrinks(query)
                searchView?.let {
                    if (!it.isIconified) it.isIconified = true
                }
                actionMenuItem.collapseActionView()
                viewModel.restoreSources()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.searchDrinks(newText)
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(
                    Screens.ABOUT,
                    "Drinks list fragment anbout text"
                )
            }
        }
        if (item.itemId == R.id.action_settings) {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(Screens.SETTINGS)
            }
        }
        return false
    }

    private fun initFAB(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
        fab.setOnClickListener {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(Screens.ADD_DRINK, -1L)
            }
        }
    }

    companion object {
        private const val EXTRA_NAME = "dlf_extra_name"

        @JvmStatic
        fun getNewInstance(name: String?): DrinksListFragment {
            val fragment = DrinksListFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            fragment.arguments = arguments
            return fragment
        }
    }
}
