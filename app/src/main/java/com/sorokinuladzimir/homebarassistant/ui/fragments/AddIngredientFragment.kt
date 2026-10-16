package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.sorokinuladzimir.homebarassistant.ui.addingredient.AddIngredientScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider
import com.sorokinuladzimir.homebarassistant.viewmodel.AddIngredientViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddIngredientFragment : Fragment(), BackButtonListener,
    AddImageDialogFragment.AddImageDialogFragmentCallback {

    private val nameState = mutableStateOf("")
    private val descriptionState = mutableStateOf("")
    private val notesState = mutableStateOf("")
    private val imagePathState = mutableStateOf<String?>(null)

    private lateinit var viewModel: AddIngredientViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_add_compose, container, false)
        viewModel = ViewModelProvider(this)[AddIngredientViewModel::class.java]
        initToolbar(rootView)

        val composeView = rootView.findViewById<ComposeView>(R.id.add_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                AddIngredientScreen(
                    imagePath = imagePathState.value,
                    name = nameState.value,
                    onNameChange = { nameState.value = it },
                    description = descriptionState.value,
                    onDescriptionChange = { descriptionState.value = it },
                    notes = notesState.value,
                    onNotesChange = { notesState.value = it },
                    onImageClick = { onImageClicked() }
                )
            }
        }
        subscribeUi()
        return rootView
    }

    private fun subscribeUi() {
        viewModel.getCurrentImagePath().observe(viewLifecycleOwner) {
            imagePathState.value = it
        }
        if (!viewModel.getIsNewIngredient()) {
            viewModel.getIngredient().observe(viewLifecycleOwner) { ingredient ->
                if (ingredient != null && viewModel.getCurrentImagePath().value == null) {
                    if (ingredient.image != null && !viewModel.getIsImageRemoved()) {
                        viewModel.getCurrentImagePath().value = ingredient.image
                    }
                    nameState.value = ingredient.name.orEmpty()
                    descriptionState.value = ingredient.description.orEmpty()
                    notesState.value = ingredient.notes.orEmpty()
                }
            }
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
            if (viewModel.getIsNewIngredient()) R.string.title_new_ingredient
            else R.string.title_ingredient_edit
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

    private fun saveIngredient() {
        viewModel.saveIngredient(nameState.value, descriptionState.value, notesState.value)
        onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_item_menu, menu)
        if (viewModel.getIsNewIngredient()) {
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
                saveIngredient()
                return true
            }
            R.id.ab_delete -> {
                val count = viewModel.deleteIngredient()
                if (count != 0) {
                    Toast.makeText(context, "Can't be deleted, used in $count cocktails", Toast.LENGTH_SHORT).show()
                }
                parentFragment?.let { (it as RouterProvider).router.navigateTo(Screens.INGREDIENTS_LIST) }
                return true
            }
            else -> Toast.makeText(context, R.string.unknown_menu_item_toast, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onBackPressed(): Boolean {
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
        fun getNewInstance(name: String?, ingredientId: Long?): AddIngredientFragment {
            val fragment = AddIngredientFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            arguments.putLong(EXTRA_ID, ingredientId ?: -1L)
            fragment.arguments = arguments
            return fragment
        }
    }
}
