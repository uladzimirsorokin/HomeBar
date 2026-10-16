package com.sorokinuladzimir.homebarassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.ui.fragments.Screens
import com.sorokinuladzimir.homebarassistant.ui.fragments.TabContainerFragment
import com.sorokinuladzimir.homebarassistant.ui.navigation.HomeBarBottomBar
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener
import com.sorokinuladzimir.homebarassistant.ui.utils.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import ru.terrakok.cicerone.commands.SystemMessage

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var searchTabFragment: TabContainerFragment? = null
    private var drinksTabFragment: TabContainerFragment? = null
    private var ingredientsTabFragment: TabContainerFragment? = null

    private val selectedTab = mutableIntStateOf(1)

    private val navigator = object : Navigator {
        override fun applyCommand(command: Command) {
            when (command) {
                is Back -> finish()
                is SystemMessage ->
                    Toast.makeText(this@MainActivity, command.message, Toast.LENGTH_SHORT).show()
                is Replace -> {
                    val fm = supportFragmentManager
                    when (command.screenKey) {
                        Screens.SEARCH -> fm.beginTransaction()
                            .detach(drinksTabFragment!!)
                            .detach(ingredientsTabFragment!!)
                            .attach(searchTabFragment!!)
                            .commitNow()
                        Screens.DRINKS -> fm.beginTransaction()
                            .detach(searchTabFragment!!)
                            .detach(ingredientsTabFragment!!)
                            .attach(drinksTabFragment!!)
                            .commitNow()
                        Screens.INGREDIENTS -> fm.beginTransaction()
                            .detach(searchTabFragment!!)
                            .detach(drinksTabFragment!!)
                            .attach(ingredientsTabFragment!!)
                            .commitNow()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_main)
        initBottomNavigation()
        initContainers()
        if (savedInstanceState == null) {
            onTabSelected(1)
        }
        checkPermissions()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        BarApp.getInstance().getNavigatorHolder().setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        BarApp.getInstance().getNavigatorHolder().removeNavigator()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
            )
        }
    }

    private fun initBottomNavigation() {
        val composeView = findViewById<ComposeView>(R.id.bottom_navigation_compose)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            HomeBarTheme(variant = homeBarThemeVariant()) {
                HomeBarBottomBar(selectedIndex = selectedTab.intValue) { onTabSelected(it) }
            }
        }
    }

    private fun onTabSelected(index: Int) {
        selectedTab.intValue = index
        val screen = when (index) {
            0 -> Screens.SEARCH
            1 -> Screens.DRINKS
            else -> Screens.INGREDIENTS
        }
        BarApp.getInstance().getRouter().replaceScreen(screen)
    }

    private fun handleBack() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is BackButtonListener && fragment.onBackPressed()) {
            return
        }
        BarApp.getInstance().getRouter().exit()
    }

    private fun initContainers() {
        val fm = supportFragmentManager

        searchTabFragment = fm.findFragmentByTag(Screens.SEARCH) as TabContainerFragment?
        if (searchTabFragment == null) {
            searchTabFragment = TabContainerFragment.getNewInstance(Screens.SEARCH)
            fm.beginTransaction()
                .add(R.id.fragment_container, searchTabFragment!!, Screens.SEARCH)
                .detach(searchTabFragment!!).commitNow()
        }

        drinksTabFragment = fm.findFragmentByTag(Screens.DRINKS) as TabContainerFragment?
        if (drinksTabFragment == null) {
            drinksTabFragment = TabContainerFragment.getNewInstance(Screens.DRINKS)
            fm.beginTransaction()
                .add(R.id.fragment_container, drinksTabFragment!!, Screens.DRINKS)
                .detach(drinksTabFragment!!).commitNow()
        }

        ingredientsTabFragment = fm.findFragmentByTag(Screens.INGREDIENTS) as TabContainerFragment?
        if (ingredientsTabFragment == null) {
            ingredientsTabFragment = TabContainerFragment.getNewInstance(Screens.INGREDIENTS)
            fm.beginTransaction()
                .add(R.id.fragment_container, ingredientsTabFragment!!, Screens.INGREDIENTS)
                .detach(ingredientsTabFragment!!).commitNow()
        }
    }
}
