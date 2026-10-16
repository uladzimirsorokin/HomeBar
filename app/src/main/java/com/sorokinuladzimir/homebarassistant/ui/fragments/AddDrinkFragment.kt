package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.db.entity.Taste
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.ui.adddrink.AddDrinkScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.ui.utils.SpinnerGlassMatcher
import com.sorokinuladzimir.homebarassistant.viewmodel.AddDrinkViewModel
import com.sorokinuladzimir.homebarassistant.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddDrinkFragment : Fragment(), BackButtonListener,
    AddImageDialogFragment.AddImageDialogFragmentCallback,
    AddTastesDialogFragment.AddTastesDialogFragmentCallback {

    private val imagePathState = mutableStateOf<String?>(null)
    private val nameState = mutableStateOf("")
    private val descriptionState = mutableStateOf("")
    private val notesState = mutableStateOf("")
    private val tastesState = mutableStateOf<List<Taste>>(emptyList())
    private val ingredientsState = mutableStateOf<List<WholeCocktail>>(emptyList())
    private val ratingState = mutableIntStateOf(0)
    private val carbonatedState = mutableStateOf(false)
    private val alcoholicState = mutableStateOf(false)
    private val glassIndexState = mutableIntStateOf(0)

    private lateinit var viewModel: AddDrinkViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_add_drink_compose, container, false)
        viewModel = ViewModelProvider(this)[AddDrinkViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        initToolbar(rootView)

        val glassNames = resources.getStringArray(R.array.glass_name_local).toList()
        val unitOptions = resources.getStringArray(R.array.ingredient_unit).toList()

        val composeView = rootView.findViewById<ComposeView>(R.id.add_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                AddDrinkScreen(
                    imagePath = imagePathState.value,
                    onImageClick = { onImageClicked() },
                    name = nameState.value,
                    onNameChange = { nameState.value = it },
                    description = descriptionState.value,
                    onDescriptionChange = { descriptionState.value = it },
                    tastes = tastesState.value.joinToString(", ") { it.text.orEmpty() },
                    onTastesClick = { showAddTastesDialog() },
                    ingredients = ingredientsState.value,
                    unitOptions = unitOptions,
                    onAmountChange = { index, value -> updateIngredient(index) { it.amount = value } },
                    onUnitChange = { index, value -> updateIngredient(index) { it.unit = value } },
                    onRemoveIngredient = { index ->
                        ingredientsState.value.getOrNull(index)?.let { viewModel.removeIngredient(it) }
                    },
                    onAddIngredients = { onAddIngredients() },
                    rating = ratingState.value,
                    onRatingChange = { ratingState.value = it },
                    carbonated = carbonatedState.value,
                    onCarbonatedChange = { carbonatedState.value = it },
                    alcoholic = alcoholicState.value,
                    onAlcoholicChange = { alcoholicState.value = it },
                    glassNames = glassNames,
                    glassIndex = glassIndexState.value,
                    onGlassChange = { glassIndexState.value = it },
                    notes = notesState.value,
                    onNotesChange = { notesState.value = it }
                )
            }
        }
        subscribeUi()
        return rootView
    }

    private fun updateIngredient(index: Int, mutate: (WholeCocktail) -> Unit) {
        val list = ingredientsState.value.toMutableList()
        list.getOrNull(index)?.let { mutate(it) }
        ingredientsState.value = list.toList()
    }

    private fun subscribeUi() {
        viewModel.getCurrentImagePath().observe(viewLifecycleOwner) {
            imagePathState.value = it
        }
        viewModel.getIngredients().observe(viewLifecycleOwner) {
            ingredientsState.value = it ?: emptyList()
        }
        viewModel.getTastesList().observe(viewLifecycleOwner) {
            tastesState.value = it ?: emptyList()
        }

        if (!viewModel.getIsNewDrink()) {
            viewModel.getDrink().observe(viewLifecycleOwner) { drink -> setInitialData(drink) }
            if (viewModel.getIngredients().value == null) {
                viewModel.getInitialIngredients().observe(viewLifecycleOwner) { ingredients ->
                    if (ingredients != null) {
                        viewModel.setInitialIngredients(ingredients, true)
                        viewModel.getInitialIngredients().removeObservers(viewLifecycleOwner)
                    }
                }
            }
        }
        viewModel.getObservableLiveIngredients().observe(viewLifecycleOwner) { ingredients ->
            if (ingredients != null) {
                viewModel.updateIngredients(ingredients, ingredientsState.value)
            }
        }
        sharedViewModel.getSelectedIds().observe(viewLifecycleOwner) { list ->
            if (list != null) {
                viewModel.setSelectedIds(list)
            }
        }
    }

    private fun setInitialData(drink: com.sorokinuladzimir.homebarassistant.db.entity.Drink?) {
        if (drink != null && viewModel.getCurrentImagePath().value == null) {
            if (drink.image != null && !viewModel.getIsImageRemoved()) {
                viewModel.getCurrentImagePath().value = drink.image
            }
            nameState.value = drink.name.orEmpty()
            descriptionState.value = drink.description.orEmpty()
            drink.tastes?.let { viewModel.getTastesList().value = it }
            ratingState.value = drink.rating
            alcoholicState.value = drink.isAlcoholic
            carbonatedState.value = drink.isCarbonated
            val glassPos = SpinnerGlassMatcher.matchGlass(
                resources.getStringArray(R.array.glass_name_local), drink.glass
            )
            glassIndexState.value = if (glassPos == -1) 0 else glassPos
            notesState.value = drink.notes.orEmpty()
        }
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        actionBar?.setTitle(
            if (viewModel.getIsNewDrink()) R.string.title_new_cocktail
            else R.string.title_edit_cocktail
        )
    }

    private fun onImageClicked() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showAddImageDialog()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
        }
    }

    private fun onAddIngredients() {
        viewModel.setIngredients(ingredientsState.value, true)
        sharedViewModel.selectIds(viewModel.getIngredientIds())
        parentFragment?.let {
            (it as RouterProvider).router.navigateTo(Screens.ADD_DRINK_INGREDIENTS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAddImageDialog()
            } else {
                Toast.makeText(context, "denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == OPEN_PICTURE_CODE && resultCode == android.app.Activity.RESULT_OK && resultData != null) {
            resultData.data?.let { viewModel.handleImage(it, DEFAULT_IMAGE_SIZE, false) }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == android.app.Activity.RESULT_OK) {
            viewModel.getPhotoUri()?.let { viewModel.handleImage(it, DEFAULT_IMAGE_SIZE, true) }
        }
    }

    private fun showAddImageDialog() {
        val newFragment: DialogFragment = AddImageDialogFragment.newInstance(
            getString(R.string.change_photo),
            viewModel.getCurrentImagePath().value != null
        )
        newFragment.setTargetFragment(this, 911)
        fragmentManager?.let { newFragment.show(it, Constants.Extra.DIALOG) }
    }

    private fun showAddTastesDialog() {
        val newFragment: DialogFragment = AddTastesDialogFragment.newInstance(
            getString(R.string.title_choose_tastes),
            viewModel.getTastes(resources.getStringArray(R.array.taste_name))
        )
        newFragment.setTargetFragment(this, 911)
        fragmentManager?.let { newFragment.show(it, Constants.Extra.DIALOG) }
    }

    override fun addTastesDialogCallback(tastes: List<Int>) {
        viewModel.setTastes(tastes, resources.getStringArray(R.array.taste_name))
    }

    override fun addImageDialogCallback(item: Int) {
        when (item) {
            0 -> dispatchTakePictureIntent()
            1 -> dispatchChoosePictureIntent()
            2 -> viewModel.removeCurrentImage()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null &&
            viewModel.createPhotoFile() != null
        ) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.getPhotoUri())
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun dispatchChoosePictureIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_PICTURE_CODE)
    }

    private fun saveDrink() {
        viewModel.saveDrink(
            nameState.value,
            descriptionState.value,
            ingredientsState.value,
            ratingState.value,
            resources.getStringArray(R.array.glass_name_local)[glassIndexState.value],
            carbonatedState.value,
            alcoholicState.value,
            notesState.value
        )
        onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_item_menu, menu)
        if (viewModel.getIsNewDrink()) {
            menu.findItem(R.id.ab_add).setTitle(R.string.menu_drink_add)
            menu.findItem(R.id.ab_delete).isVisible = false
        } else {
            menu.findItem(R.id.ab_add).setTitle(R.string.menu_save)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.ab_add -> {
                saveDrink()
                return true
            }
            R.id.ab_delete -> {
                viewModel.deleteDrink()
                parentFragment?.let { (it as RouterProvider).router.navigateTo(Screens.DRINKS_LIST) }
                return true
            }
            else -> Toast.makeText(context, R.string.unknown_menu_item_toast, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onBackPressed(): Boolean {
        sharedViewModel.selectIds(null)
        parentFragment?.let { (it as RouterProvider).router.exit() }
        return true
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_ID = "extra_id"
        private const val OPEN_PICTURE_CODE = 2
        private const val REQUEST_PERMISSION_CODE = 3
        private const val DEFAULT_IMAGE_SIZE = 750

        @JvmStatic
        fun getNewInstance(name: String?, drinkId: Long?): AddDrinkFragment {
            val fragment = AddDrinkFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            arguments.putLong(EXTRA_ID, drinkId ?: -1L)
            fragment.arguments = arguments
            return fragment
        }
    }
}
