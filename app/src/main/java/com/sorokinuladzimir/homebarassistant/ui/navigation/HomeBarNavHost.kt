package com.sorokinuladzimir.homebarassistant.ui.navigation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.sorokinuladzimir.homebarassistant.BarDataRepository.QueryType
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.ResourcesActivity
import com.sorokinuladzimir.homebarassistant.core.ui.components.AddImageDialog
import com.sorokinuladzimir.homebarassistant.core.ui.components.AddTastesDialog
import com.sorokinuladzimir.homebarassistant.core.ui.components.rememberImagePicker
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import com.sorokinuladzimir.homebarassistant.ui.about.AboutScreen
import com.sorokinuladzimir.homebarassistant.ui.adddrink.AddDrinkScreen
import com.sorokinuladzimir.homebarassistant.ui.addingredient.AddIngredientScreen
import com.sorokinuladzimir.homebarassistant.ui.addingredients.SelectIngredientsScreen
import com.sorokinuladzimir.homebarassistant.ui.drinkdetail.DrinkDetailScreen
import com.sorokinuladzimir.homebarassistant.ui.drinks.DrinksListScreen
import com.sorokinuladzimir.homebarassistant.ui.ingredientdetail.IngredientDetailScreen
import com.sorokinuladzimir.homebarassistant.ui.ingredients.IngredientsListScreen
import com.sorokinuladzimir.homebarassistant.ui.search.FoundDrinksScreen
import com.sorokinuladzimir.homebarassistant.ui.search.SearchDrinksScreen
import com.sorokinuladzimir.homebarassistant.ui.settings.SettingsScreen
import com.sorokinuladzimir.homebarassistant.ui.utils.SpinnerGlassMatcher
import com.sorokinuladzimir.homebarassistant.ui.utils.ThemeUtils
import com.sorokinuladzimir.homebarassistant.viewmodel.AddDrinkIngredientsViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.AddDrinkViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.AddIngredientViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.DrinkListViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.DrinkViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.FoundDrinksViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.IngredientListViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.IngredientViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.RemoteDrinkViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.SharedViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

private fun Context.findActivity(): ComponentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun NavGraphBuilder.homeBarGraph(navController: NavController) {
    composable(
        Routes.FOUND_DRINKS,
        arguments = listOf(
            navArgument(Routes.ARG_CONDITIONS) {
                type = NavType.StringType; defaultValue = ""; nullable = true
            }
        )
    ) { FoundDrinksRoute(navController, it) }

    composable(Routes.SEARCH_FILTER) { SearchFilterRoute(navController) }
    composable(Routes.DRINKS_LIST) { DrinksListRoute(navController) }
    composable(Routes.INGREDIENTS_LIST) { IngredientsListRoute(navController) }

    composable(
        Routes.REMOTE_DRINK,
        arguments = listOf(
            navArgument(Routes.ARG_ENTITY) {
                type = NavType.StringType; defaultValue = ""; nullable = true
            }
        )
    ) { RemoteDrinkRoute(navController) }

    composable(
        Routes.LOCAL_DRINK,
        arguments = listOf(
            navArgument(Routes.ARG_ID) { type = NavType.LongType },
            navArgument(Routes.ARG_EDITABLE) { type = NavType.BoolType; defaultValue = false }
        )
    ) { LocalDrinkRoute(navController, it) }

    composable(
        Routes.ADD_DRINK,
        arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType; defaultValue = -1L })
    ) { AddDrinkRoute(navController) }

    composable(Routes.ADD_DRINK_INGREDIENTS) { AddDrinkIngredientsRoute(navController) }

    composable(
        Routes.LOCAL_INGREDIENT,
        arguments = listOf(
            navArgument(Routes.ARG_ID) { type = NavType.LongType },
            navArgument(Routes.ARG_EDITABLE) { type = NavType.BoolType; defaultValue = false }
        )
    ) { IngredientDetailRoute(navController, it) }

    composable(
        Routes.ADD_INGREDIENT,
        arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType; defaultValue = -1L })
    ) { AddIngredientRoute(navController) }

    composable(
        Routes.ABOUT,
        arguments = listOf(
            navArgument(Routes.ARG_TEXT) {
                type = NavType.StringType; defaultValue = ""; nullable = true
            }
        )
    ) { AboutRoute(navController, it) }

    composable(Routes.SETTINGS) { SettingsRoute(navController) }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.menu_search)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun FoundDrinksRoute(navController: NavController, entry: NavBackStackEntry) {
    val viewModel: FoundDrinksViewModel = hiltViewModel()
    val conditions = entry.arguments?.getString(Routes.ARG_CONDITIONS).orEmpty()
    val drinks by viewModel.getDrinks().observeAsState()

    LaunchedEffect(conditions) {
        viewModel.searchDrinks(conditions, QueryType.SEARCH_BY_CONDITIONS, true)
    }

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.search_cocktails_toolbar_title),
                actions = { AboutSettingsMenu(navController) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.SEARCH_FILTER) }) {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            FoundDrinksScreen(
                drinks = drinks.orEmpty(),
                emptyMessage = stringResource(R.string.no_items_found),
                onItemClick = { navController.navigate(Routes.remoteDrink(json.encodeToString(it))) },
                onLoadMore = { viewModel.searchDrinks(conditions, QueryType.CURRENT, false) }
            )
        }
    }
}

@Composable
private fun SearchFilterRoute(navController: NavController) {
    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.search_cocktails_toolbar_title),
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            SearchDrinksScreen(
                onSearch = { path ->
                    navController.navigate(Routes.foundDrinks(path)) {
                        popUpTo(Routes.FOUND_DRINKS) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun DrinksListRoute(navController: NavController) {
    val viewModel: DrinkListViewModel = hiltViewModel()
    val drinks by viewModel.getDrinks().observeAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.restoreSources() }

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.drinks_list_fragment_title),
                actions = { AboutSettingsMenu(navController) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.addDrink(-1L)) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SearchField(query) { query = it; viewModel.searchDrinks(it) }
            Box(Modifier.weight(1f)) {
                DrinksListScreen(
                    drinks = drinks.orEmpty(),
                    emptyMessage = stringResource(R.string.no_items_drinks),
                    onItemClick = { navController.navigate(Routes.localDrink(it.id ?: -1L, true)) }
                )
            }
        }
    }
}

@Composable
private fun IngredientsListRoute(navController: NavController) {
    val viewModel: IngredientListViewModel = hiltViewModel()
    val ingredients by viewModel.getIngredients().observeAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.restoreSources() }

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.ingredients_list_toolbar_title),
                actions = { AboutSettingsMenu(navController) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.addIngredient(-1L)) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SearchField(query) { query = it; viewModel.searchIngredients(it) }
            Box(Modifier.weight(1f)) {
                IngredientsListScreen(
                    ingredients = ingredients.orEmpty(),
                    emptyMessage = stringResource(R.string.no_items_ingredients),
                    onItemClick = { navController.navigate(Routes.localIngredient(it.id ?: -1L, true)) }
                )
            }
        }
    }
}

@Composable
private fun LocalDrinkRoute(navController: NavController, entry: NavBackStackEntry) {
    val viewModel: DrinkViewModel = hiltViewModel()
    val id = entry.arguments?.getLong(Routes.ARG_ID) ?: -1L
    val editable = entry.arguments?.getBoolean(Routes.ARG_EDITABLE) ?: false
    val drink by viewModel.getDrink().observeAsState()
    val ingredients by viewModel.getIngredients().observeAsState()

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = drink?.name.orEmpty(),
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            if (editable) {
                FloatingActionButton(onClick = { navController.navigate(Routes.addDrink(id)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            DrinkDetailScreen(
                drink = drink,
                ingredients = ingredients.orEmpty(),
                showNotes = true,
                onIngredientClick = {
                    navController.navigate(Routes.localIngredient(it.ingredientId ?: -1L, false))
                }
            )
        }
    }
}

@Composable
private fun IngredientDetailRoute(navController: NavController, entry: NavBackStackEntry) {
    val viewModel: IngredientViewModel = hiltViewModel()
    val id = entry.arguments?.getLong(Routes.ARG_ID) ?: -1L
    val editable = entry.arguments?.getBoolean(Routes.ARG_EDITABLE) ?: false
    val ingredient by viewModel.getIngredient().observeAsState()
    val relatedDrinks by viewModel.getRelatedDrinks().observeAsState()

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = ingredient?.name.orEmpty(),
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            if (editable) {
                FloatingActionButton(onClick = { navController.navigate(Routes.addIngredient(id)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            IngredientDetailScreen(
                ingredient = ingredient,
                relatedDrinks = relatedDrinks.orEmpty(),
                onDrinkClick = { navController.navigate(Routes.localDrink(it.id ?: -1L, false)) }
            )
        }
    }
}

@Composable
private fun RemoteDrinkRoute(navController: NavController) {
    val viewModel: RemoteDrinkViewModel = hiltViewModel()
    val drink by viewModel.getDrink().observeAsState()
    val ingredients by viewModel.getDrinkIngredients().observeAsState()

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = drink?.name.orEmpty(),
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveDrink()
                navController.popBackStack()
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            DrinkDetailScreen(
                drink = drink,
                ingredients = ingredients.orEmpty(),
                showNotes = false,
                onIngredientClick = {},
                onImageLoaded = { viewModel.setBitmap(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDrinkRoute(navController: NavController) {
    val viewModel: AddDrinkViewModel = hiltViewModel()
    val context = LocalContext.current
    val activity = context.findActivity()
    val sharedViewModel: SharedViewModel = hiltViewModel(activity!!)

    val imagePath by viewModel.getCurrentImagePath().observeAsState()
    val ingredients by viewModel.getIngredients().observeAsState()
    val tastes by viewModel.getTastesList().observeAsState()
    val drink by viewModel.getDrink().observeAsState()
    val initialIngredients by viewModel.getInitialIngredients().observeAsState()
    val liveIngredients by viewModel.getObservableLiveIngredients().observeAsState()
    val sharedSelection by sharedViewModel.getSelectedIds().observeAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var carbonated by remember { mutableStateOf(false) }
    var alcoholic by remember { mutableStateOf(false) }
    var glassIndex by remember { mutableIntStateOf(0) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showTastesDialog by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf<List<WholeCocktail>>(emptyList()) }

    val glassNames = remember { context.resources.getStringArray(R.array.glass_name_local).toList() }
    val unitOptions = remember { context.resources.getStringArray(R.array.ingredient_unit).toList() }

    LaunchedEffect(drink) {
        val d = drink ?: return@LaunchedEffect
        if (viewModel.getCurrentImagePath().value == null) {
            if (d.image != null && !viewModel.getIsImageRemoved()) {
                viewModel.getCurrentImagePath().value = d.image
            }
            name = d.name.orEmpty()
            description = d.description.orEmpty()
            notes = d.notes.orEmpty()
            rating = d.rating
            carbonated = d.isCarbonated
            alcoholic = d.isAlcoholic
            glassIndex = SpinnerGlassMatcher.matchGlass(
                context.resources.getStringArray(R.array.glass_name_local), d.glass
            ).coerceAtLeast(0)
        }
    }
    LaunchedEffect(initialIngredients) {
        if (!viewModel.getIsNewDrink() && initialIngredients != null &&
            viewModel.getIngredients().value == null
        ) {
            viewModel.setInitialIngredients(initialIngredients!!, true)
        }
    }
    LaunchedEffect(liveIngredients) {
        if (liveIngredients != null) {
            viewModel.updateIngredients(liveIngredients!!, working)
        }
    }
    LaunchedEffect(sharedSelection) {
        if (sharedSelection != null) viewModel.setSelectedIds(sharedSelection!!)
    }
    LaunchedEffect(ingredients) { working = ingredients.orEmpty() }

    val imagePicker = rememberImagePicker(
        onPicked = { uri, deleteSource -> viewModel.handleImage(uri, DEFAULT_IMAGE_SIZE, deleteSource) },
        createPhotoUri = { viewModel.createPhotoFile() }
    )

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(
                    if (viewModel.getIsNewDrink()) R.string.title_new_cocktail
                    else R.string.title_edit_cocktail
                ),
                onBack = {
                    sharedViewModel.selectIds(null)
                    navController.popBackStack()
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveDrink(
                            name, description, working, rating,
                            glassNames[glassIndex], carbonated, alcoholic, notes
                        )
                        sharedViewModel.selectIds(null)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                    }
                    if (!viewModel.getIsNewDrink()) {
                        IconButton(onClick = {
                            viewModel.deleteDrink()
                            navController.navigate(Routes.DRINKS_LIST) {
                                popUpTo(Routes.DRINKS_LIST) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AddDrinkScreen(
                imagePath = imagePath,
                onImageClick = { showImageDialog = true },
                name = name,
                onNameChange = { name = it },
                description = description,
                onDescriptionChange = { description = it },
                tastes = tastes.orEmpty().joinToString(", ") { it.text.orEmpty() },
                onTastesClick = { showTastesDialog = true },
                ingredients = working,
                unitOptions = unitOptions,
                onAmountChange = { index, value ->
                    working = working.toMutableList().also { it.getOrNull(index)?.amount = value }
                },
                onUnitChange = { index, value ->
                    working = working.toMutableList().also { it.getOrNull(index)?.unit = value }
                },
                onRemoveIngredient = { index ->
                    working.getOrNull(index)?.let { viewModel.removeIngredient(it) }
                },
                onAddIngredients = {
                    viewModel.setIngredients(working, true)
                    sharedViewModel.selectIds(viewModel.getIngredientIds())
                    navController.navigate(Routes.ADD_DRINK_INGREDIENTS)
                },
                rating = rating,
                onRatingChange = { rating = it },
                carbonated = carbonated,
                onCarbonatedChange = { carbonated = it },
                alcoholic = alcoholic,
                onAlcoholicChange = { alcoholic = it },
                glassNames = glassNames,
                glassIndex = glassIndex,
                onGlassChange = { glassIndex = it },
                notes = notes,
                onNotesChange = { notes = it }
            )
            if (showImageDialog) {
                AddImageDialog(
                    title = stringResource(R.string.change_photo),
                    allowDelete = imagePath != null,
                    onTakePhoto = { showImageDialog = false; imagePicker.takePhoto() },
                    onChooseFromLibrary = { showImageDialog = false; imagePicker.chooseFromLibrary() },
                    onRemove = { showImageDialog = false; viewModel.removeCurrentImage() },
                    onDismiss = { showImageDialog = false }
                )
            }
            if (showTastesDialog) {
                AddTastesDialog(
                    title = stringResource(R.string.title_choose_tastes),
                    tastes = context.resources.getStringArray(R.array.taste_name).toList(),
                    initialSelection = viewModel.getTastes(context.resources.getStringArray(R.array.taste_name)),
                    onConfirm = { selection ->
                        showTastesDialog = false
                        viewModel.setTastes(selection, context.resources.getStringArray(R.array.taste_name))
                    },
                    onDismiss = { showTastesDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIngredientRoute(navController: NavController) {
    val viewModel: AddIngredientViewModel = hiltViewModel()
    val context = LocalContext.current

    val imagePath by viewModel.getCurrentImagePath().observeAsState()
    val ingredient by viewModel.getIngredient().observeAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ingredient) {
        val i = ingredient ?: return@LaunchedEffect
        if (viewModel.getCurrentImagePath().value == null) {
            if (i.image != null && !viewModel.getIsImageRemoved()) {
                viewModel.getCurrentImagePath().value = i.image
            }
            name = i.name.orEmpty()
            description = i.description.orEmpty()
            notes = i.notes.orEmpty()
        }
    }

    val imagePicker = rememberImagePicker(
        onPicked = { uri, deleteSource -> viewModel.handleImage(uri, DEFAULT_IMAGE_SIZE, deleteSource) },
        createPhotoUri = { viewModel.createPhotoFile() }
    )

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(
                    if (viewModel.getIsNewIngredient()) R.string.title_new_ingredient
                    else R.string.title_ingredient_edit
                ),
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveIngredient(name, description, notes)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                    }
                    if (!viewModel.getIsNewIngredient()) {
                        IconButton(onClick = {
                            viewModel.deleteIngredient()
                            navController.navigate(Routes.INGREDIENTS_LIST) {
                                popUpTo(Routes.INGREDIENTS_LIST) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AddIngredientScreen(
                imagePath = imagePath,
                name = name,
                onNameChange = { name = it },
                description = description,
                onDescriptionChange = { description = it },
                notes = notes,
                onNotesChange = { notes = it },
                onImageClick = { showImageDialog = true }
            )
            if (showImageDialog) {
                AddImageDialog(
                    title = stringResource(R.string.change_photo),
                    allowDelete = imagePath != null,
                    onTakePhoto = { showImageDialog = false; imagePicker.takePhoto() },
                    onChooseFromLibrary = { showImageDialog = false; imagePicker.chooseFromLibrary() },
                    onRemove = { showImageDialog = false; viewModel.removeCurrentImage() },
                    onDismiss = { showImageDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDrinkIngredientsRoute(navController: NavController) {
    val viewModel: AddDrinkIngredientsViewModel = hiltViewModel()
    val context = LocalContext.current
    val sharedViewModel: SharedViewModel = hiltViewModel(context.findActivity()!!)

    val ingredients by viewModel.getIngredients().observeAsState()
    val localSelection by viewModel.getLocalSelection().observeAsState()
    val sharedSelection by sharedViewModel.getSelectedIds().observeAsState()

    var query by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(localSelection) {
        if (!localSelection.isNullOrEmpty()) selectedIds = localSelection!!.toSet()
    }
    LaunchedEffect(sharedSelection) {
        if (!sharedSelection.isNullOrEmpty() && viewModel.getLocalSelection().value.isNullOrEmpty()) {
            viewModel.setLocalSelection(sharedSelection!!)
        }
    }

    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.title_choose_ingredients),
                onBack = {
                    viewModel.setLocalSelection(selectedIds.toList())
                    navController.popBackStack()
                },
                actions = {
                    IconButton(onClick = {
                        sharedViewModel.selectIds(selectedIds.toList())
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.addIngredient(-1L)) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            SelectIngredientsScreen(
                ingredients = ingredients.orEmpty().filter {
                    query.isBlank() || it.name?.contains(query, ignoreCase = true) == true
                },
                query = query,
                onQueryChange = { query = it },
                selectedIds = selectedIds,
                onToggle = { item ->
                    item.id?.let { id ->
                        val set = selectedIds.toMutableSet()
                        if (!set.add(id)) set.remove(id)
                        selectedIds = set
                    }
                },
                emptyMessage = stringResource(R.string.no_items_ingredients)
            )
        }
    }
}

@Composable
private fun AboutRoute(navController: NavController, entry: NavBackStackEntry) {
    val text = entry.arguments?.getString(Routes.ARG_TEXT).orEmpty()
    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.menu_about),
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AboutScreen(
                aboutText = text,
                onResources = {
                    navController.context.startActivity(
                        Intent(navController.context, ResourcesActivity::class.java)
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingsRoute(navController: NavController) {
    val activity = LocalContext.current.findActivity()
    val currentTheme = activity?.getPreferences(Context.MODE_PRIVATE)
        ?.getInt(Constants.Extra.APP_THEME, 0) ?: 0
    Scaffold(
        topBar = {
            HomeBarTopBar(
                title = stringResource(R.string.settings_fragment_title),
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            SettingsScreen(
                currentThemeId = currentTheme,
                onThemeSelected = { theme -> activity?.let { ThemeUtils.changeToTheme(it, theme) } }
            )
        }
    }
}

private const val DEFAULT_IMAGE_SIZE = 750
