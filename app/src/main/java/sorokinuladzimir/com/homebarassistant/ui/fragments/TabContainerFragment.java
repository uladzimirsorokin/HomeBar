package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.terrakok.cicerone.Cicerone;
import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.Router;
import ru.terrakok.cicerone.android.SupportFragmentNavigator;
import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.LocalCiceroneHolder;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;

/**
 * Created by sorok on 18.10.2017.
 */

public class TabContainerFragment extends Fragment implements RouterProvider, BackButtonListener {

    private static final String EXTRA_NAME = "tcf_extra_name";

    private Navigator navigator;
    LocalCiceroneHolder ciceroneHolder = new LocalCiceroneHolder();

    public static TabContainerFragment getNewInstance(String name) {
        TabContainerFragment fragment = new TabContainerFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private String getContainerName() {
        return getArguments().getString(EXTRA_NAME);
    }

    private Cicerone<Router> getCicerone() {
        return ciceroneHolder.getCicerone(getContainerName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_tab_container, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getChildFragmentManager().findFragmentById(R.id.ftc_container) == null) {
            switch (getContainerName()){
                case Screens.SEARCH:
                    getCicerone().getRouter().replaceScreen(Screens.FOUND_DRINKS);
                    break;
                case Screens.DRINKS:
                    getCicerone().getRouter().replaceScreen(Screens.DRINKS_LIST);
                    break;
                case Screens.INGREDIENTS:
                    getCicerone().getRouter().replaceScreen(Screens.INGREDIENTS_LIST);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getCicerone().getNavigatorHolder().setNavigator(getNavigator());
    }

    @Override
    public void onPause() {
        getCicerone().getNavigatorHolder().removeNavigator();
        super.onPause();
    }

    private Navigator getNavigator() {
        if (navigator == null) {
            navigator = new SupportFragmentNavigator(getChildFragmentManager(), R.id.ftc_container) {

                @Override
                protected Fragment createFragment(String screenKey, Object data) {
                    switch (screenKey){
                        case Screens.FOUND_DRINKS:
                            return FoundDrinksFragment.getNewInstance(getContainerName(),(Bundle) data);
                        case Screens.SEARCH_DRINKS:
                            return SearchDrinksFragment.getNewInstance(getContainerName());
                        case Screens.DRINKS_LIST:
                            return DrinksListFragment.getNewInstance(getContainerName());
                        case Screens.INGREDIENTS_LIST:
                            return IngredientsListFragment.getNewInstance(getContainerName());
                        case Screens.SINGLE_DRINK:
                            return SingleDrinkFragment.getNewInstance(getContainerName(),(Bundle) data);
                        case Screens.ADD_DRINK:
                            return AddDrinkFragment.getNewInstance(getContainerName(),(Long) data);
                        case Screens.LOCAL_DRINK:
                            return LocalDrinkFragment.getNewInstance(getContainerName(), (Long) data);
                        case Screens.LOCAL_INGREDIENT:
                            return IngredientFragment.getNewInstance(getContainerName(), (Long) data);
                        case Screens.ADD_INGREDIENT:
                            return AddIngredientFragment.getNewInstance(getContainerName(), (Long) data);
                        case Screens.ADD_DRINK_INGREDIENTS:
                            return AddDrinkIngredientsFragment.getNewInstance(getContainerName());
                        case Screens.ABOUT:
                            return AboutFragment.getNewInstance(getContainerName(), (String) data);
                        default:
                            return null;
                    }
                }

                @Override
                protected void showSystemMessage(String message) {

                }

                @Override
                protected void exit() {

                }
            };
        }
        return navigator;
    }

    @Override
    public Router getRouter() {
        return getCicerone().getRouter();
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.ftc_container);
        if (fragment != null
                && fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed()) {
            return true;
        } else {
            BarApp.getInstance().getRouter().exit();
            return true;
        }
    }

}
