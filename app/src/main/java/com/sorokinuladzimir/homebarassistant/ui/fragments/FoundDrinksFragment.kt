package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.os.Bundle
import android.text.TextUtils
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
import com.sorokinuladzimir.homebarassistant.BarDataRepository.QueryType
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import com.sorokinuladzimir.homebarassistant.ui.search.FoundDrinksScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.FoundDrinksViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoundDrinksFragment : Fragment() {

    private val drinksState = mutableStateOf<List<DrinkEntity>>(emptyList())
    private lateinit var viewModel: FoundDrinksViewModel
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var searchView: SearchView? = null
    private var requestConditions: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_list_compose, container, false)
        requestConditions = savedInstanceState?.getString(Constants.Extra.CONDITIONS)

        initToolbar(rootView)
        initFab(rootView)

        swipeRefresh = rootView.findViewById(R.id.swipe_refresh_layout)
        swipeRefresh.setOnRefreshListener {
            requestConditions?.let { conditions ->
                swipeRefresh.isRefreshing = true
                searchDrinks(conditions, QueryType.CURRENT, true)
            }
            swipeRefresh.isRefreshing = false
        }

        val composeView = rootView.findViewById<ComposeView>(R.id.list_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                FoundDrinksScreen(
                    drinks = drinksState.value,
                    emptyMessage = stringResource(R.string.no_items_found),
                    onItemClick = { navigateToDrink(it) },
                    onLoadMore = { searchDrinks(requestConditions, QueryType.CURRENT, false) }
                )
            }
        }

        viewModel = ViewModelProvider(this)[FoundDrinksViewModel::class.java]
        viewModel.getDrinks().observe(viewLifecycleOwner) { drinks ->
            if (drinks != null) {
                swipeRefresh.isRefreshing = false
                drinksState.value = drinks
            }
        }

        val args = arguments?.getBundle(EXTRA_BUNDLE)
        if (args != null && !args.isEmpty) {
            requestConditions = args.getString(Constants.Extra.REQUEST_CONDITIONS)
            swipeRefresh.isRefreshing = true
            searchDrinks(requestConditions, QueryType.SEARCH_BY_CONDITIONS, true)
            args.clear()
        }
        if (TextUtils.isEmpty(requestConditions)) {
            setDefaultCocktailsList()
        }
        return rootView
    }

    private fun searchDrinks(query: String?, searchType: QueryType, clearList: Boolean) {
        viewModel.searchDrinks(query ?: "", searchType, clearList)
    }

    private fun setDefaultCocktailsList() {
        if (requestConditions == null) {
            requestConditions = ""
            swipeRefresh.isRefreshing = true
            searchDrinks(requestConditions, QueryType.SEARCH_BY_CONDITIONS, true)
        }
    }

    private fun navigateToDrink(drink: DrinkEntity) {
        parentFragment?.let {
            val bundle = Bundle()
            bundle.putSerializable(Constants.Extra.COCKTAIL, drink)
            (it as RouterProvider).router.navigateTo(Screens.SINGLE_DRINK, bundle)
        }
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setTitle(R.string.search_cocktails_toolbar_title)
    }

    private fun initFab(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_search))
        fab.setOnClickListener {
            parentFragment?.let { (it as RouterProvider).router.navigateTo(Screens.SEARCH_DRINKS) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu)
        val actionMenuItem = menu.findItem(R.id.action_search)
        searchView = actionMenuItem.actionView as SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                requestConditions = query
                swipeRefresh.isRefreshing = true
                searchDrinks(requestConditions, QueryType.SEARCH_BY_NAME, true)
                searchView?.let { if (!it.isIconified) it.isIconified = true }
                actionMenuItem.collapseActionView()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean = false
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
            parentFragment?.let { (it as RouterProvider).router.navigateTo(Screens.SETTINGS) }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.Extra.CONDITIONS, requestConditions)
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_BUNDLE = "extra_bundle"

        @JvmStatic
        fun getNewInstance(name: String?, bundle: Bundle?): FoundDrinksFragment {
            val fragment = FoundDrinksFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            if (bundle != null) arguments.putBundle(EXTRA_BUNDLE, bundle)
            fragment.arguments = arguments
            return fragment
        }
    }
}
