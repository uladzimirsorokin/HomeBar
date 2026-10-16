package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.ui.addingredients.SelectIngredientsScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.AddDrinkIngredientsViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddDrinkIngredientsFragment : Fragment(), BackButtonListener {

    private val ingredientsState = mutableStateOf<List<Ingredient>>(emptyList())
    private val selectedIdsState = mutableStateOf<Set<Long>>(emptySet())
    private val queryState = mutableStateOf("")

    private lateinit var viewModel: AddDrinkIngredientsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_list_compose, container, false)
        viewModel = ViewModelProvider(this)[AddDrinkIngredientsViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        initToolbar(rootView)
        initFab(rootView)

        val swipeRefresh = rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefresh.setOnRefreshListener { swipeRefresh.isRefreshing = false }

        val composeView = rootView.findViewById<ComposeView>(R.id.list_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                SelectIngredientsScreen(
                    ingredients = ingredientsState.value.filter {
                        queryState.value.isBlank() ||
                            it.name?.contains(queryState.value, ignoreCase = true) == true
                    },
                    query = queryState.value,
                    onQueryChange = { queryState.value = it },
                    selectedIds = selectedIdsState.value,
                    onToggle = { toggleSelection(it) },
                    emptyMessage = stringResource(R.string.no_items_ingredients)
                )
            }
        }
        subscribeUi()
        return rootView
    }

    private fun subscribeUi() {
        viewModel.getIngredients().observe(viewLifecycleOwner) {
            ingredientsState.value = it ?: emptyList()
        }
        sharedViewModel.getSelectedIds().observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty() && viewModel.getLocalSelection().value.isNullOrEmpty()) {
                viewModel.setLocalSelection(list)
            }
        }
        viewModel.getLocalSelection().observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                selectedIdsState.value = list.toSet()
            }
        }
    }

    private fun toggleSelection(ingredient: Ingredient) {
        val id = ingredient.id ?: return
        val selected = selectedIdsState.value.toMutableSet()
        if (!selected.add(id)) {
            selected.remove(id)
        }
        selectedIdsState.value = selected
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(
            R.string.title_choose_ingredients
        )
    }

    private fun initFab(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
        fab.setOnClickListener {
            parentFragment?.let {
                (it as RouterProvider).router.navigateTo(Screens.ADD_INGREDIENT, null)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_drink_ingredient_menu, menu)
        menu.findItem(R.id.action_search)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_about -> parentFragment?.let {
                (it as RouterProvider).router.navigateTo(Screens.ABOUT, "")
            }
            R.id.ab_add -> {
                sharedViewModel.selectIds(selectedIdsState.value.toList())
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        viewModel.setLocalSelection(selectedIdsState.value.toList())
    }

    override fun onBackPressed(): Boolean {
        parentFragment?.let { (it as RouterProvider).router.exit() }
        return true
    }

    companion object {
        private const val EXTRA_NAME = "adif_extra_name"

        @JvmStatic
        fun getNewInstance(name: String?): AddDrinkIngredientsFragment {
            val fragment = AddDrinkIngredientsFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            fragment.arguments = arguments
            return fragment
        }
    }
}
