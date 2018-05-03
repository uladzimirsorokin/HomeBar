package sorokinuladzimir.com.homebarassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.tbruyelle.rxpermissions2.RxPermissions;

import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.commands.Back;
import ru.terrakok.cicerone.commands.Command;
import ru.terrakok.cicerone.commands.Replace;
import ru.terrakok.cicerone.commands.SystemMessage;
import sorokinuladzimir.com.homebarassistant.ui.fragments.Screens;
import sorokinuladzimir.com.homebarassistant.ui.fragments.TabContainerFragment;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.utils.ThemeUtils;


public class MainActivity extends AppCompatActivity {

    private AHBottomNavigation bottomNavigation;
    private TabContainerFragment searchTabFragment;
    private TabContainerFragment drinksTabFragment;
    private TabContainerFragment ingredientsTabFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtils.onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_main);

        initBottomNavigation();
        initContainers();

        if (savedInstanceState == null) {
            bottomNavigation.setCurrentItem(1);
        }

        checkPermissons();
    }


    private Navigator navigator = new Navigator() {
        @Override
        public void applyCommand(Command command) {
            if (command instanceof Back) {
                finish();
            } else if (command instanceof SystemMessage) {
                Toast.makeText(MainActivity.this, ((SystemMessage) command).getMessage(), Toast.LENGTH_SHORT).show();
            } else if (command instanceof Replace) {
                FragmentManager fm = getSupportFragmentManager();

                switch (((Replace) command).getScreenKey()) {
                    case Screens.SEARCH:
                        fm.beginTransaction()
                                .detach(drinksTabFragment)
                                .detach(ingredientsTabFragment)
                                .attach(searchTabFragment)
                                .commitNow();
                        break;
                    case Screens.DRINKS:
                        fm.beginTransaction()
                                .detach(searchTabFragment)
                                .detach(ingredientsTabFragment)
                                .attach(drinksTabFragment)
                                .commitNow();
                        break;
                    case Screens.INGREDIENTS:
                        fm.beginTransaction()
                                .detach(searchTabFragment)
                                .detach(drinksTabFragment)
                                .attach(ingredientsTabFragment)
                                .commitNow();
                        break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        BarApp.getInstance().getNavigatorHolder().setNavigator(navigator);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BarApp.getInstance().getNavigatorHolder().removeNavigator();
    }

    @SuppressLint("CheckResult")
    private void checkPermissons() {
        RxPermissions rxPermissions = new RxPermissions(this);
        // Must be done during an initialization phase like onCreate
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {

                    } else {
                        Toast.makeText(this,"Permisson denied, photo capture disabled",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initBottomNavigation(){
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setDefaultBackgroundColor(fetchPrimaryColor());
        bottomNavigation.setAccentColor(Color.parseColor("#FFFFFF"));
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(getString(R.string.bottom_bar_search), R.drawable.ic_search);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(getString(R.string.bottom_bar_drinks), R.drawable.cocktail);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(getString(R.string.bottom_bar_ingredients), R.drawable.bottles);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            switch (position){
                case 0:
                    BarApp.getInstance().getRouter().replaceScreen(Screens.SEARCH);
                    break;
                case 1:
                    BarApp.getInstance().getRouter().replaceScreen(Screens.DRINKS);
                    break;
                case 2:
                    BarApp.getInstance().getRouter().replaceScreen(Screens.INGREDIENTS);
                    break;
            }

            return true;
        });
        bottomNavigation.setOnNavigationPositionListener(y -> {
            // Manage the new y position
        });
    }

    private int fetchPrimaryColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null
                && fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed()) {
            return;
        } else {
            BarApp.getInstance().getRouter().exit();
        }
    }

    private void initContainers() {
        FragmentManager fm = getSupportFragmentManager();
        searchTabFragment = (TabContainerFragment) fm.findFragmentByTag(Screens.SEARCH);
        if (searchTabFragment == null) {
            searchTabFragment = TabContainerFragment.getNewInstance(Screens.SEARCH);
            fm.beginTransaction()
                    .add(R.id.fragment_container, searchTabFragment, Screens.SEARCH)
                    .detach(searchTabFragment).commitNow();
        }

        drinksTabFragment = (TabContainerFragment) fm.findFragmentByTag(Screens.DRINKS);
        if (drinksTabFragment == null) {
            drinksTabFragment = TabContainerFragment.getNewInstance(Screens.DRINKS);
            fm.beginTransaction()
                    .add(R.id.fragment_container, drinksTabFragment, Screens.DRINKS)
                    .detach(drinksTabFragment).commitNow();
        }

        ingredientsTabFragment = (TabContainerFragment) fm.findFragmentByTag(Screens.INGREDIENTS);
        if (ingredientsTabFragment == null) {
            ingredientsTabFragment = TabContainerFragment.getNewInstance(Screens.INGREDIENTS);
            fm.beginTransaction()
                    .add(R.id.fragment_container, ingredientsTabFragment, Screens.INGREDIENTS)
                    .detach(ingredientsTabFragment).commitNow();
        }
    }
}
