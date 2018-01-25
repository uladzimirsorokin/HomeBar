package sorokinuladzimir.com.homebarassistant;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.commands.Back;
import ru.terrakok.cicerone.commands.Command;
import ru.terrakok.cicerone.commands.Replace;
import ru.terrakok.cicerone.commands.SystemMessage;
import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.fragments.Screens;
import sorokinuladzimir.com.homebarassistant.ui.fragments.TabContainerFragment;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;


public class MainActivity extends AppCompatActivity {

    private AHBottomNavigation bottomNavigation;
    private TabContainerFragment searchTabFragment;
    private TabContainerFragment drinksTabFragment;
    private TabContainerFragment ingredientsTabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBottomNavigation();
        initContainers();

        if (savedInstanceState == null) {
            bottomNavigation.setCurrentItem(1);
        }
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

    private void initBottomNavigation(){
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        bottomNavigation.setAccentColor(Color.parseColor("#FFFFFF"));
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("search", R.drawable.ic_search);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("drinks", R.drawable.cocktail);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("ingredients", R.drawable.bottles);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
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
            }
        });
        bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override public void onPositionChange(int y) {
                // Manage the new y position
            }
        });
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
