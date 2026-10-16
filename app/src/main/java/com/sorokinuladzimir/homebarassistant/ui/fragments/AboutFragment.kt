package com.sorokinuladzimir.homebarassistant.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.ResourcesActivity
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.ui.about.AboutScreen
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider

class AboutFragment : Fragment(), BackButtonListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fr_about, container, false)
        initToolbar(rootView)

        val aboutText = arguments?.getString(EXTRA_TEXT).orEmpty()
        val composeView = rootView.findViewById<ComposeView>(R.id.about_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = requireContext().homeBarThemeVariant()) {
                AboutScreen(
                    aboutText = aboutText,
                    onResources = {
                        startActivity(Intent(requireContext(), ResourcesActivity::class.java))
                    }
                )
            }
        }
        return rootView
    }

    private fun initToolbar(view: View) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.menu_about)
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
        private const val EXTRA_TEXT = "extra_text"

        @JvmStatic
        fun getNewInstance(name: String?, aboutText: String?): AboutFragment {
            val fragment = AboutFragment()
            val arguments = Bundle()
            arguments.putString(EXTRA_NAME, name)
            arguments.putString(EXTRA_TEXT, aboutText)
            fragment.arguments = arguments
            return fragment
        }
    }
}
